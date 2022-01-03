package io.zephyr.breeze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class BreezeTest {

  @Test
  void ensureVerticesAreRegisteredCorrectly() {
    val result = Breeze.newTaskGraph(TestReduction.class, TaskA.class).getExecutionGraph();
    assertEquals(3, result.vertexCount());
  }

  @Test
  void ensureConflictingProcessDefinitionsAreRejected() {
    @Reduction
    class A {}

    @Reduction
    class B {}

    assertThrows(IllegalArgumentException.class, () -> Breeze.newTaskGraph(A.class, B.class));
  }

  @Test
  void ensureBreezeCreatesCorrectTaskGraphForSingleTask() {
    @Task
    class DefaultTask {

      String value;

      @Run
      void run() {
        this.value = "run";
      }
    }

    val graph = Breeze.newTaskGraph(DefaultTask.class);
    assertTrue(
        graph.getExecutionGraph().edgeSet().stream()
            .anyMatch(edge -> edge.getLabel().getKey().equals(DefaultTask.class.getName())));
  }

  @Reduction(displayName = "my test reduction", coalesce = true, parallel = true)
  public static class TestReduction {}

  @Task
  public static class TaskB {

    private String value;

    @Run
    void run() {
      this.value = "set";
    }
  }

  @Task(displayName = "task A")
  @Dependency(type = TaskB.class)
  public static class TaskA {

    private String value;

    @Run
    void doStuff() {
      this.value = "also set";
    }
  }
}
