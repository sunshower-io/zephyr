package io.sunshower.kernel.concurrency;

import java.util.concurrent.*;

@SuppressWarnings("PMD.DoNotUseThreads")
public class ExecutorWorkerPool implements WorkerPool {
  final ExecutorService executorService;
  final ExecutorService kernelExecutorService;

  public ExecutorWorkerPool(ExecutorService executorService) {
    this.executorService = executorService;
    // this can't really rely on a single thread as the task graph will frequently block it, but we
    // don't need that many of them
    // this should probably be configurable
    kernelExecutorService =
        new ThreadPoolExecutor(0, 5, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
  }

  @Override
  public <T> Future<T> submit(Callable<T> value) {
    return executorService.submit(value);
  }

  @Override
  public <K> void submitKernelAllocated(Runnable result) {
    kernelExecutorService.submit(result);
  }
}
