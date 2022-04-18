package io.zephyr.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Graph;
import io.sunshower.gyre.Schedule;
import io.sunshower.gyre.Scope;
import io.sunshower.lang.events.EventListener;
import io.zephyr.api.Disposable;
import lombok.NonNull;

public interface Process<E> extends Schedule<DirectedGraph.Edge<E>, Task> {

  enum Mode {
    KernelAllocated,
    SingleThreaded,
    UserspaceAllocated,
  }

  Mode getMode();

  void setMode(@NonNull Mode mode);

  /** if true, remove redundant edges (may improve execution time) */
  boolean coalesce();

  /** @return true if this process should be scheduled on a parallel scheduler */
  boolean isParallel();

  /** @return the context for this computation */
  Scope getContext();

  Graph<DirectedGraph.Edge<E>, Task> getExecutionGraph();

  /**
   * Note that listeners are automatically cleared once the process is complete
   *
   * @param type the type of the task event listener to add
   * @param listener the listener
   * @return a disposable
   */
  Disposable addEventListener(TaskEventType type, EventListener<Task> listener);
}
