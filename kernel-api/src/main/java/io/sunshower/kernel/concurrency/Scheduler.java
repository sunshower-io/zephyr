package io.sunshower.kernel.concurrency;

public interface Scheduler<K> {
  TaskTracker<K> submit(Process<K> process);
}
