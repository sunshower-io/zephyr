package io.sunshower.kernel.concurrency;

import io.sunshower.kernel.core.KernelException;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import lombok.val;

@SuppressWarnings("PMD.AvoidUsingVolatile")
public class MultichannelCapableScheduler implements Scheduler {

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
    this.running = true;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void stop(String channel) {
    synchronized (lock) {
      val module = processors.get(channel);
      module.processQueue.clear();
      module.stop();
      processors.remove(channel);
    }
  }

  @Override
  public void start(String channel) {
    synchronized (lock) {
      val module = processors.get(channel);
      executorService.submit(module);
    }
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
    synchronized (lock) {
      processors.computeIfAbsent(processor.getChannel(), ProcessingModule::new).register(processor);
    }
  }

  @Override
  public void unregisterHandler(Processor processor) {
    synchronized (lock) {
      processors.remove(processor.getChannel());
    }
  }

  @Override
  public boolean scheduleTask(ConcurrentProcess action) {
    synchronized (lock) {
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
      // can this possibly deadlock with start()
      synchronized (lock) {
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
            processQueue.take();
          }
        } catch (InterruptedException ex) {
          throw new KernelException(ex);
        }
      }
    }

    void stop() {
      running = false;
    }

    @Override
    public void run() {
      start();
    }
  }
}
