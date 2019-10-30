package io.sunshower.kernel.concurrency;

import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
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
@SuppressWarnings("PMD.AvoidUsingVolatile")
public class MultichannelCapableScheduler implements Scheduler {

  static final String MAINTENANCE_CHANNEL = "kernel:scheduler:maintenance";

  private final Object lock = new Object();
  static final Logger log = Logging.get(MultichannelCapableScheduler.class);

  private volatile boolean running;
  private final ExecutorService executorService;
  private final Map<String, ProcessingModule> processors;

  public MultichannelCapableScheduler(ExecutorService service) {
    processors = new HashMap<>();
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
    registerHandler(
        new Processor() {
          @Override
          public String getChannel() {
            return MAINTENANCE_CHANNEL;
          }

          @Override
          public void process(ConcurrentProcess process) {
            process.perform();
          }
        });
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
    running = false;
    while (!processors.isEmpty()) {
      processors.values().removeIf(module -> module.processQueue.isEmpty());
    }
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
    scheduleTask(
        new ConcurrentProcess() {
          @Override
          public String getChannel() {
            return MAINTENANCE_CHANNEL;
          }

          @Override
          public void perform() {
            val module = processors.get(channel);
            if (module.processors.remove(processor)) {
              log.log(Level.INFO, "kernel.scheduler.processor.unregistered", channel);
            }
            if (module.processors.isEmpty()) {
              log.log(Level.INFO, "kernel.scheduler.module.noprocessors", channel);
              module.stop();
              // log after this because the thread may've already exited
              log.log(Level.INFO, "kernel.channel.stopped", channel);
            }
          }
        });
  }

  @Override
  public boolean scheduleTask(ConcurrentProcess action) {
    synchronized (processors) { // all operations on processors must use processors' monitor
      val module = processors.get(action.getChannel());
      if (module == null) {
        throw new IllegalStateException(
            "Error: cannot schedule a task because no processor for it exists ");
      }
      return module.processQueue.offer(action);
    }
  }

  @SuppressWarnings("PMD.DoNotUseThreads")
  static class ProcessingModule implements Runnable {
    final Object lock = new Object();

    volatile boolean running;
    private final String channel;
    private final List<Processor> processors;
    private final TransferQueue<ConcurrentProcess> processQueue;

    public ProcessingModule(final String channel) {
      this.channel = channel;
      this.processors = new LinkedList<>();
      this.processQueue = new LinkedTransferQueue<>();
    }

    void register(Processor processor) {
      // can this possibly deadlock with start() if synchronized on lock()?
      // yes--synchronize on processors
      synchronized (processors) {
        processors.add(processor);
      }
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    void start() {
      running = true;
      while (running) {
        try {
          synchronized (lock) {
            ConcurrentProcess action;
            do {
              action = processQueue.peek();
              Thread.yield();
            } while (action == null);
            for (val processor : processors) {
              processor.process(action);
            }
            processQueue
                .take(); // must take from queue after processors have processed task so that
            // await() and awaitShutdown() block correctly
          }
        } catch (InterruptedException ex) {
          return;
        }
      }
    }

    void stop() {
      log.log(Level.INFO, "kernel.channel.stopping", channel);
      running = false;
    }

    @Override
    public void run() {
      start();
    }
  }
}
