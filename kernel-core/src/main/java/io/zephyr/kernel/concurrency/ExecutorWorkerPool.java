package io.zephyr.kernel.concurrency;

import java.util.concurrent.*;

@SuppressWarnings("PMD.DoNotUseThreads")
public class ExecutorWorkerPool implements WorkerPool {

  final ExecutorService executorService;
  final ExecutorService kernelExecutorService;

  public ExecutorWorkerPool(
      ExecutorService executorService, final ExecutorService kernelExecutorService) {
    this.executorService = executorService;
    // this can't really rely on a single thread as the task graph will frequently block it, but we
    // don't need that many of them
    // this should probably be configurable
    this.kernelExecutorService = kernelExecutorService;
    //        new ThreadPoolExecutor(0, 5, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
  }

  @Override
  public ExecutorService getKernelExecutor() {
    return kernelExecutorService;
  }

  @Override
  public ExecutorService getUserspaceExecutor() {
    return executorService;
  }

  @Override
  public <T> Future<T> submit(Callable<T> value) {
    return executorService.submit(value);
  }

  @Override
  public <T> Future<T> submitKernelAllocated(Callable<T> value) {
    return kernelExecutorService.submit(value);
  }

  @Override
  public <K> void submitKernelAllocated(Runnable result) {
    kernelExecutorService.submit(result);
  }
}
