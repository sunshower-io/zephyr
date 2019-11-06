package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Schedule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Process<K> {

  final Schedule<DirectedGraph.Edge<K>, Task> tasks;

  public Schedule<DirectedGraph.Edge<K>, Task> getTasks() {
    return tasks;
  }
}
