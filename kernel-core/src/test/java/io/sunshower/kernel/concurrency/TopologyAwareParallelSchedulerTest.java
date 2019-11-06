package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.SerialScheduler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TopologyAwareParallelSchedulerTest {

  private Context context;
  private TaskGraph<String> graph;


  @BeforeEach
  void setUp() {
    graph = new TaskGraph<>();
    context = new Context();
  }

  @Test
  void ensureSimpleTasksWork() throws ExecutionException, InterruptedException {
    TaskGraph<String> g = new TaskGraph<>();
    List<String> results = new ArrayList<>();

    var scheduler =
        new TopologyAwareParallelScheduler(
            new ExecutorWorkerPool(Executors.newFixedThreadPool(10)));

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
    result.get();
    assertEquals(results.get(0), "b");
    assertEquals(results.get(1), "a");
  }

  abstract static class NamedTask implements Task {
    final String name;

    protected NamedTask(String name) {
      this.name = name;
    }
  }
}
