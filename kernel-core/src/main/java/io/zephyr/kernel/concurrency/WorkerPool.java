package io.zephyr.kernel.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@SuppressWarnings("PMD.DoNotUseThreads")
public interface WorkerPool {

  ExecutorService getKernelExecutor();

  <T> Future<T> submit(Callable<T> value);

  <K> void submitKernelAllocated(Runnable result);
}
