package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sunshower.gyre.DirectedGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.AvoidDuplicateLiterals"
})
class TopologyAwareParallelSchedulerTest {

  private Context context;
  private ReductionScope scope;
  private TaskGraph<String> graph;
  private TopologyAwareParallelScheduler<String> scheduler;

  @BeforeEach
  void setUp() {
    graph = new TaskGraph<>();
    scope = ReductionScope.newRoot(context = ReductionScope.newContext());
    scheduler =
        new TopologyAwareParallelScheduler<>(
            new ExecutorWorkerPool(Executors.newFixedThreadPool(1)));
  }

  @Test
  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
  void ensureDependentTaskRetrievesPredecessorValueForSimpleTaskGraph()
      throws ExecutionException, InterruptedException {
    graph.connect(
        new NamedTask("a") {
          @Override
          public TaskValue run(Context context) {
            assertEquals(context.get("test"), "hello world!", "message should be correct");
            return null;
          }
        },
        new NamedTask("b") {
          @Override
          public TaskValue run(Context context) {
            context.set("test", "hello world!");
            return null;
          }
        },
        DirectedGraph.outgoing("a dependsOn b"));

    val task = scheduler.submit(scheduleFrom(graph), scope);
    task.get();
  }

  private Process<String> scheduleFrom(TaskGraph<String> graph) {
    return new DefaultProcess<>("test", false, false, ReductionScope.newContext(), graph);
  }

  @Test
  void ensureSimpleTasksWork() throws ExecutionException, InterruptedException {
    TaskGraph<String> g = new TaskGraph<>();
    List<String> results = new ArrayList<>();

    var scheduler =
        new TopologyAwareParallelScheduler(new ExecutorWorkerPool(Executors.newFixedThreadPool(1)));

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

    //    var topoSchedule = new SerialScheduler<DirectedGraph.Edge<String>, Task>().apply(g);
    //    var process = new Process<>(topoSchedule);
    Process<String> process = null;
    var result = scheduler.submit(process, ReductionScope.newRoot(context));
    result.get();
    assertEquals(results.get(0), "b", "be must be first");
    assertEquals(results.get(1), "a", "must be second");
  }

  abstract static class NamedTask implements Task {
    final String name;

    protected NamedTask(String name) {
      this.name = name;
    }
  }
}
