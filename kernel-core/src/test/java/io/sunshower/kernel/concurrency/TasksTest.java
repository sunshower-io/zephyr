package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.ParallelScheduler;
import io.sunshower.kernel.core.lifecycle.KernelClassLoaderCreationPhase;
import io.sunshower.kernel.core.lifecycle.KernelModuleListReadPhase;
import io.sunshower.kernel.lifecycle.processes.KernelFilesystemCreatePhase;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TasksTest {

  ReductionScope scope;
  Context context;
  private ParallelScheduler<DirectedGraph.Edge<String>, Task> scheduler;

  @BeforeEach
  void setUp() {
    context = ReductionScope.newContext();
    scope = ReductionScope.newRoot(context);
    scheduler = new ParallelScheduler<>();
  }

  @Test
  void ensureProcessBuilderWithSimpleTaskProducesCorrectGraph() {
    val task = mock(Task.class);
    val process = Tasks.newProcess("a").withContext(scope).register("test", task).create();
    val graph = scheduler.apply(process.getExecutionGraph());
    assertEquals(graph.size(), 1);
    val t = graph.get(0).getTasks().get(0);
    assertNotNull(t, "task must not be null");
  }

  @Test
  void ensureFailingProcessConfigurationWorks() {
    val process =
        Tasks.newProcess("kernel:start:filesystem")
            .withContext(context)
            // a
            .register("kernel:lifecycle:module:list", new KernelModuleListReadPhase())
            // b
            .register("kernel:lifecycle:filesystem:create", new KernelFilesystemCreatePhase())
            // c
            .register("kernel:lifecycle:classloader", new KernelClassLoaderCreationPhase())
            .task("kernel:lifecycle:module:list")
            .dependsOn("kernel:lifecycle:filesystem:create")
            .task("kernel:lifecycle:classloader")
            .dependsOn("kernel:lifecycle:module:list")
            .create();
    val s = scheduler.apply(process.getExecutionGraph());

    assertEquals(s.size(), 3, "must have 3 items");
  }

  @Test
  void ensureWithContextDoesntChangeExecutionGraph() {
    val taskA = mock(Task.class);
    val taskB = mock(Task.class);
    val taskC = mock(Task.class);
    val proc =
        Tasks.newProcess("s")
            .withContext(context)
            .register("a", taskA)
            .register("b", taskB)
            .register("c", taskC)
            .task("a")
            .dependsOn("b")
            .task("b")
            .dependsOn("c")
            .create();

    val s = scheduler.apply(proc.getExecutionGraph());
    assertEquals(s.size(), 3);
    assertEquals(((NamedTask) s.get(0).getTasks().get(0).getValue()).name, "c");
    assertEquals(((NamedTask) s.get(1).getTasks().get(0).getValue()).name, "b");
    assertEquals(((NamedTask) s.get(2).getTasks().get(0).getValue()).name, "a");
  }

  @Test
  void ensureSimpleLinearDependenciesWork() {
    val taskA = mock(Task.class);
    val taskB = mock(Task.class);
    val taskC = mock(Task.class);
    val proc =
        Tasks.newProcess("s")
            .register("a", taskA)
            .register("b", taskB)
            .register("c", taskC)
            .task("a")
            .dependsOn("b")
            .task("b")
            .dependsOn("c")
            .create();

    val s = scheduler.apply(proc.getExecutionGraph());
    assertEquals(s.size(), 3);
    assertEquals(((NamedTask) s.get(0).getTasks().get(0).getValue()).name, "c");
    assertEquals(((NamedTask) s.get(1).getTasks().get(0).getValue()).name, "b");
    assertEquals(((NamedTask) s.get(2).getTasks().get(0).getValue()).name, "a");
  }
}
