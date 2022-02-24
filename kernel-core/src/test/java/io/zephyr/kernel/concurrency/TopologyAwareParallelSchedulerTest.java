package io.zephyr.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Scope;
import io.zephyr.kernel.concurrency.Process.Mode;
import io.zephyr.kernel.events.EventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated("concurrent registrations by other tests fuck this up")
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
            new ExecutorWorkerPool(
                Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor()));
  }

  @RepeatedTest(1000)
  @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "rawtypes", "unchecked"})
  void ensureListenerWorks() throws ExecutionException, InterruptedException {
    graph.connect(
        new Task("a") {
          @SneakyThrows
          @Override
          public TaskValue run(Scope scope) {
            System.out.println("ONE");
            assertEquals(scope.get("test"), "hello world!", "message should be correct");
            return null;
          }
        },
        new Task("b") {
          @Override
          @SneakyThrows
          public TaskValue run(Scope scope) {
            System.out.println("TWO");
            scope.set("test", "hello world!");
            return null;
          }
        },
        DirectedGraph.outgoing("a dependsOn b"));

    val listener = mock(EventListener.class);
    val schedule = scheduleFrom(graph);
    schedule.setMode(Mode.UserspaceAllocated);
    val registration = schedule.addEventListener(TaskEvents.TASK_COMPLETE, listener);
    var task = scheduler.submit(schedule, scope);
    task.toCompletableFuture().get();
    verify(listener, timeout(500).times(2)).onEvent(eq(TaskEvents.TASK_COMPLETE), any());
    registration.dispose();
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
        new TopologyAwareParallelScheduler(
            new ExecutorWorkerPool(
                Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(2)));

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
