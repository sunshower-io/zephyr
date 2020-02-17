package io.zephyr.kernel.concurrency;

import java.util.concurrent.ExecutorService;
import javax.inject.Inject;

@SuppressWarnings("PMD.DoNotUseThreads")
public final class KernelScheduler<K> implements Scheduler<K> {

  final WorkerPool workerPool;
  private final TopologyAwareParallelScheduler<K> scheduler;

  @Inject
  public KernelScheduler(WorkerPool pool) {
    this.workerPool = pool;
    this.scheduler = new TopologyAwareParallelScheduler<>(workerPool);
  }

  @Override
  public ExecutorService getKernelExecutor() {
    return workerPool.getKernelExecutor();
  }

  @Override
  public TaskTracker<K> submit(Process<K> process) {
    return scheduler.submit(process, process.getContext());
  }
}
