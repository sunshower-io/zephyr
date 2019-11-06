package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Graph;
import io.sunshower.gyre.Schedule;

public interface Process<E> extends Schedule<DirectedGraph.Edge<E>, Task> {

  /** if true, remove redundant edges (may improve execution time) */
  boolean coalesce();

  /** @return true if this process should be scheduled on a parallel scheduler */
  boolean isParallel();

  /** @return the context for this computation */
  Context getContext();

  Graph<DirectedGraph.Edge<E>, Task> getExecutionGraph();
}
