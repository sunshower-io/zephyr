package io.sunshower.kernel.concurrency;

import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import lombok.val;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

public class BlockingBoundedThreadFactory implements ThreadFactory {

  static final Logger log = Logging.get(BlockingBoundedThreadFactory.class, "Concurrency");

  final int maxParallelism;
  final Object lock = new Object();

  volatile int currentParallelism;
  final BlockingQueue<Thread> threadQueue;

  public BlockingBoundedThreadFactory(int maxParallelism) {
    this.maxParallelism = maxParallelism;
    threadQueue = new ArrayBlockingQueue<>(maxParallelism);
    log.log(Level.INFO, "threadfactory.starting", maxParallelism);
  }

  @Override
  public Thread newThread(Runnable r) {
    var thread = threadQueue.poll();
    if (thread == null) {
      synchronized (lock) {
        thread = threadQueue.poll();
        if (thread == null) {
          log.log(Level.INFO, "threadfactory.cachedthread.miss");
          if (currentParallelism < maxParallelism) {
            log.log(Level.INFO, "threadfactory.newthread", currentParallelism, maxParallelism);
            val result = new ThreadReturningRunnable(r);
            currentParallelism++;
            return result.submit();
          } else {
            log.log(Level.INFO, "threadfactory.waiting", currentParallelism, maxParallelism);
            Thread result = null;
            for (; ; ) {

              result = threadQueue.poll();
              if (result != null) {
                break;
              }
            }
            log.log(Level.INFO, "threadfactory.donewaiting");
            return result;
          }
        }
      }
    }
    log.log(Level.INFO, "threadfactory.cachedthread.success");
    return thread;
  }

  final class ThreadReturningRunnable implements Runnable {
    final Runnable delegate;
    final Object lock = new Object();
    volatile Thread thread;

    ThreadReturningRunnable(Runnable delegate) {
      this.delegate = delegate;
    }

    @Override
    public void run() {
      try {
        System.out.println("BEFORE");
        delegate.run();
        System.out.println("AFTER");
      } finally {
        System.out.println("DONE");
        log.log(Level.INFO, "threadfactory.returning");
        threadQueue.add(thread);
        currentParallelism--;
      }
    }

    synchronized Thread submit() {
      thread = new Thread(this);
      return thread;
    }
  }
}
