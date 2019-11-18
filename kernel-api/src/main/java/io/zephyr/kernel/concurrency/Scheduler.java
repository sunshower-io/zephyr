package io.zephyr.kernel.concurrency;

public interface Scheduler<K> {
  TaskTracker<K> submit(Process<K> process);
}
