package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.AvoidDuplicateLiterals"
})
class TopologyAwareParallelSchedulerTest {

  private Scope scope;
  private TaskGraph<String> graph;
  private TopologyAwareParallelScheduler<String> scheduler;

  @BeforeEach
  void setUp() {
    graph = new TaskGraph<>();
    scope = Scope.root();
    scheduler =
        new TopologyAwareParallelScheduler<>(
            new ExecutorWorkerPool(Executors.newFixedThreadPool(1)));
  }

  @Test
  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
  void ensureDependentTaskRetrievesPredecessorValueForSimpleTaskGraph()
      throws ExecutionException, InterruptedException {
    graph.connect(
        new Task("a") {
          @Override
          public TaskValue run(Scope scope) {
            assertEquals(scope.get("test"), "hello world!", "message should be correct");
            return null;
          }
        },
        new Task("b") {
          @Override
          public TaskValue run(Scope scope) {
            scope.set("test", "hello world!");
            return null;
          }
        },
        DirectedGraph.outgoing("a dependsOn b"));

    var task = scheduler.submit(scheduleFrom(graph), scope);
    task.get();
  }

  @Test
  void ensureSimpleTasksWork() throws ExecutionException, InterruptedException {
    TaskGraph<String> g = new TaskGraph<>();
    List<String> results = new ArrayList<>();

    var scheduler =
        new TopologyAwareParallelScheduler(new ExecutorWorkerPool(Executors.newFixedThreadPool(1)));

    g.connect(
        new Task("a") {
          @Override
          public Task.TaskValue run(Scope scope) {
            results.add(name);
            return null;
          }
        },
        new Task("b") {

          @Override
          public Task.TaskValue run(Scope scope) {
            results.add(name);
            return null;
          }
        },
        DirectedGraph.incoming("a dependsOn b"));
    var process = scheduleFrom(g);
    var result = scheduler.submit(process, Scope.root());
    result.get();
    assertEquals(results.get(0), "b", "be must be first");
    assertEquals(results.get(1), "a", "must be second");
  }

  private Process<String> scheduleFrom(TaskGraph<String> graph) {
    return new DefaultProcess<String>("test", false, false, Scope.root(), graph);
  }
}
