package io.zephyr.kernel.concurrency;

import java.util.concurrent.ExecutorService;

public interface Scheduler<K> {

  ExecutorService getKernelExecutor();

  TaskTracker<K> submit(Process<K> process);
}
