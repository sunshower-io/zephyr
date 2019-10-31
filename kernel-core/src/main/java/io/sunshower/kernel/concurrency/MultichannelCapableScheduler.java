package io.sunshower.kernel.concurrency;

import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import lombok.val;

/**
 * Process verification:
 *
 * <p>Main Thread: MT
 *
 * <p>MT:scheduler.start() start() acquires processors:lock via registerHandler() registerHandler
 * acquires lock:lock via start(channel) MT holds lock:lock, processors:lock for all of start() and
 * releases
 */
@SuppressWarnings({
  "PMD.AvoidUsingVolatile",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidLiteralsInIfCondition"
})
public class MultichannelCapableScheduler implements Scheduler {

  static final String MAINTENANCE_CHANNEL = "kernel:scheduler:maintenance";

  private final Object lock = new Object();
  static final Logger log = Logging.get(MultichannelCapableScheduler.class);

  private volatile boolean running;
  private final ExecutorService executorService;
  private final Map<String, ProcessingModule> processors;

  public MultichannelCapableScheduler(ExecutorService service) {
    processors = new ConcurrentHashMap<>();
    this.executorService = service;
  }

  @Override
  public void await(String channel) {
    synchronized (lock) {
      val module = processors.get(channel);
      while (!module.processQueue.isEmpty()) {
        Thread.yield();
      }
    }
  }

  @Override
  @SuppressFBWarnings
  public void synchronize() {
    for (; ; ) {
      synchronized (lock) {
        boolean allDone = true;
        for (val entry : processors.values()) {
          allDone &= entry.processQueue.isEmpty();
        }
        if (allDone) {
          return;
        }
      }
    }
  }

  @Override
  public void terminate() {
    synchronized (lock) {
      running = false;
      log.log(Level.INFO, "scheduler.terminating");
      val iter = processors.values().iterator();
      while (iter.hasNext()) {
        val module = iter.next();
        log.log(Level.INFO, "scheduler.terminating.module", module.channel);
        module.processQueue.clear();
        iter.remove();
        log.log(Level.INFO, "scheduler.terminated.module", module.channel);
      }
    }
  }

  @Override
  public void start() {
    log.log(Level.INFO, "kernel.scheduler.starting");
    this.running = true;
    log.log(Level.INFO, "kernel.scheduler.started");
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void stop(String channel) {
    log.log(Level.INFO, "kernel.scheduler.stopping");
    synchronized (lock) {
      val module = processors.get(channel);
      module.processQueue.clear();
      module.stop();
      processors.remove(channel);
    }
    log.log(Level.INFO, "kernel.scheduler.stopped");
  }

  @Override
  public void start(String channel) {
    log.log(Level.INFO, "kernel.scheduler.channel.starting", channel);
    synchronized (lock) {
      val module = processors.get(channel);
      executorService.submit(module);
    }
    log.log(Level.INFO, "kernel.scheduler.channel.started", channel);
  }

  @Override
  public void awaitShutdown() {
    while (!processors.isEmpty()) {
      synchronized (lock) {
        processors.values().removeIf(next -> next.processQueue.isEmpty());
      }
    }
    running = false;
  }

  @Override
  public boolean cancel(ConcurrentProcess action) {
    synchronized (lock) {
      val module = processors.get(action.getChannel());
      if (module == null) {
        return false;
      }
      return module.processQueue.remove(action);
    }
  }

  @Override
  public void registerHandler(Processor processor) {
    synchronized (processors) {
      boolean requiresStart = !processors.containsKey(processor.getChannel());
      processors.computeIfAbsent(processor.getChannel(), ProcessingModule::new).register(processor);
      if (requiresStart) {
        start(processor.getChannel());
      }
    }
  }

  @Override
  public void unregisterHandler(Processor processor) {
    val channel = processor.getChannel();
    log.log(Level.INFO, "kernel.scheduler.processor.unregistering", channel);
    // need to schedule a maintenance task to avoid synchronizing on processors from another thread
    synchronized (processors) {
      val module = processors.get(channel);
      if (module != null) {
        module.processors.remove(processor);
        if (module.processors.isEmpty()) {
          processors.remove(channel);
        }
      }
    }
  }

  @Override
  public boolean scheduleTask(ConcurrentProcess action) {
    synchronized (processors) { // all operations on processors must use processors' monitor
      val module = processors.get(action.getChannel());
      if (module == null) {
        throw new IllegalStateException(
            "Error: cannot schedule a task because no processor for it exists ");
      }
      return module.enqueue(action);
    }
  }

  @SuppressWarnings("PMD.DoNotUseThreads")
  class ProcessingModule implements Runnable {
    final Object lock = new Object();

    volatile boolean running;
    private final String channel;
    private final Set<Processor> processors;
    private final BlockingQueue<ConcurrentProcess> processQueue;

    public ProcessingModule(final String channel) {
      this.channel = channel;
      this.processors = new HashSet<>();
      this.processQueue = new LinkedBlockingQueue<>();
    }

    void register(Processor processor) {
      synchronized (processors) {
        if (!processors.add(processor)) {
          log.warning("processor already registered");
        }
      }
    }

    boolean enqueue(ConcurrentProcess process) {
      log.log(Level.INFO, "scheduler.enqueuing", channel, process.getChannel());
      return processQueue.offer(process);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    void start() {
      while (running) {
        synchronized (lock) {
          log.log(Level.FINE, channel, Thread.currentThread().getName());
          if (processors.isEmpty()) {
            running = false;
            MultichannelCapableScheduler.this.processors.remove(channel);
          }

          val action = processQueue.peek();
          if (action == null) {
            continue;
          }
          for (val processor : processors) {
            processor.process(action);
          }
          // must remove from queue to ensure processors have time to act
          processQueue.poll();
        }
      }
    }

    void stop() {
      log.log(Level.INFO, "kernel.channel.stopping", channel);
      running = false;
    }

    @Override
    public void run() {
      log.log(Level.INFO, "scheduler.channel.starting", channel);
      running = true;
      start();
    }
  }
}
