package io.sunshower.kernel.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorWorkerPool implements WorkerPool {
  final ExecutorService executorService;
  final ExecutorService kernelExecutorService;

  public ExecutorWorkerPool(ExecutorService executorService) {
    this.executorService = executorService;
    kernelExecutorService = Executors.newFixedThreadPool(1);
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
