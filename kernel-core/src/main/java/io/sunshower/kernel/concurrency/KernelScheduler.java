package io.sunshower.kernel.concurrency;

import javax.inject.Inject;

public final class KernelScheduler<K> implements Scheduler<K> {

  final WorkerPool workerPool;
  private final TopologyAwareParallelScheduler<K> scheduler;

  @Inject
  public KernelScheduler(WorkerPool pool) {
    this.workerPool = pool;
    this.scheduler = new TopologyAwareParallelScheduler<>(workerPool);
  }

  @Override
  public TaskTracker<K> submit(Process<K> process) {
    return scheduler.submit(process, process.getContext());
  }
}
