package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.assertNull;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.SerialScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TopologyAwareParallelSchedulerTest {

  @BeforeEach
  void setUp() {}

  @Test
  void ensureSimpleTasksWork() throws ExecutionException, InterruptedException {
    TaskGraph<String> g = new TaskGraph<>();
    List<String> results = new ArrayList<>();

    var scheduler =
        new TopologyAwareParallelScheduler(
            new ExecutorWorkerPool(Executors.newFixedThreadPool(10)));

    for (int i = 0; i < 100; i++) {
      g.connect(
          new NamedTask("a") {
            @Override
            public Task.TaskValue run(Context c) {
              results.add(name);
              return null;
            }
          },
          new NamedTask("b") {

            @Override
            public Task.TaskValue run(Context c) {
              results.add(name);
              return null;
            }
          },
          DirectedGraph.incoming("a dependsOn b"));

      var topoSchedule = new SerialScheduler<DirectedGraph.Edge<String>, Task>().apply(g);
      var process = new Process<>(topoSchedule);
      var result = scheduler.submit(process, new Context());

    }
  }

  abstract static class NamedTask implements Task {
    final String name;

    protected NamedTask(String name) {
      this.name = name;
    }
  }
}
