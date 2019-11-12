package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.ParallelScheduler;
import io.sunshower.gyre.Scope;
import io.sunshower.kernel.core.lifecycle.KernelClassLoaderCreationPhase;
import io.sunshower.kernel.core.lifecycle.KernelFilesystemCreatePhase;
import io.sunshower.kernel.core.lifecycle.KernelModuleListReadPhase;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class TasksTest {

  private Scope context;
  private ParallelScheduler<DirectedGraph.Edge<String>, Task> scheduler;

  @BeforeEach
  void setUp() {
    context = Scope.root();
    scheduler = new ParallelScheduler<>();
  }

  @Test
  void ensureProcessBuilderWithSimpleTaskProducesCorrectGraph() {
    val task = new T("a");
    val process = Tasks.newProcess("a").withContext(context).register(task).create();
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
            .register(new KernelModuleListReadPhase("kernel:lifecycle:module:list"))

            // b
            .register(new KernelFilesystemCreatePhase("kernel:lifecycle:filesystem:create"))
            // c
            .register(new KernelClassLoaderCreationPhase("kernel:lifecycle:classloader"))
            .task("kernel:lifecycle:module:list")
            .dependsOn("kernel:lifecycle:filesystem:create")
            .task("kernel:lifecycle:classloader")
            .dependsOn("kernel:lifecycle:module:list")
            .create();
    val s = scheduler.apply(process.getExecutionGraph());
    assertEquals(s.size(), 3, "must have 3 items");
  }

  static class T extends Task {

    protected T(String name) {
      super(name);
    }

    @Override
    public TaskValue run(Scope scope) {
      return null;
    }
  }

  @Test
  void ensureWithContextDoesntChangeExecutionGraph() {
    val taskA = new T("a");
    val taskB = new T("b");
    val taskC = new T("c");
    val proc =
        Tasks.newProcess("s")
            .withContext(context)
            .register(taskA)
            .register(taskB)
            .register(taskC)
            .task("a")
            .dependsOn("b")
            .task("b")
            .dependsOn("c")
            .create();

    val s = scheduler.apply(proc.getExecutionGraph());
    assertEquals(s.size(), 3);
    assertEquals(s.get(0).getTasks().get(0).getValue().name, "c");
    assertEquals(s.get(1).getTasks().get(0).getValue().name, "b");
    assertEquals(s.get(2).getTasks().get(0).getValue().name, "a");
  }

  @Test
  void ensureSimpleLinearDependenciesWork() {
    val taskA = new T("a");
    val taskB = new T("b");
    val taskC = new T("c");
    val proc =
        Tasks.newProcess("s")
            .register(taskA)
            .register(taskB)
            .register(taskC)
            .task("a")
            .dependsOn("b")
            .task("b")
            .dependsOn("c")
            .create();

    val s = scheduler.apply(proc.getExecutionGraph());
    assertEquals(s.size(), 3);
    assertEquals((s.get(0).getTasks().get(0).getValue()).name, "c");
    assertEquals((s.get(1).getTasks().get(0).getValue()).name, "b");
    assertEquals((s.get(2).getTasks().get(0).getValue()).name, "a");
  }
}
