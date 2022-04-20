package io.zephyr.kernel.concurrency;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import lombok.extern.java.Log;

@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.AvoidThreadGroup"})
@Log
public class NamedThreadFactory implements ThreadFactory, UncaughtExceptionHandler {

  private static final AtomicInteger poolNumber = new AtomicInteger(1);
  private final ThreadGroup group;
  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final String namePrefix;

  public NamedThreadFactory(String prefix) {
    group = Thread.currentThread().getThreadGroup();
    namePrefix = prefix + "-" + poolNumber.getAndIncrement() + "-thread-";
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
    t.setUncaughtExceptionHandler(this);
    if (t.isDaemon()) {
      t.setDaemon(false);
    }
    if (t.getPriority() != Thread.NORM_PRIORITY) {
      t.setPriority(Thread.NORM_PRIORITY);
    }
    return t;
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    log.log(Level.WARNING, "Exception in thread {0}: {1}",
        new Object[]{thread.getName(), throwable.getMessage()});
  }
}
