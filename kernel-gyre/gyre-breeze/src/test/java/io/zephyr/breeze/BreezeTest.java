package io.zephyr.breeze;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;


class BreezeTest {
  @Reduction(
      displayName = "my test reduction",
      coalesce = true,
      parallel = true
  )
  public static class TestReduction {

  }

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

  @Test
  void ensureVerticesAreRegisteredCorrectly() {
    val result = Breeze.newTaskGraph(TestReduction.class, TaskA.class);
    assertEquals(3, result.vertexCount());
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
    assertTrue(graph.edgeSet().stream()
        .anyMatch(edge -> edge.getLabel().equals(DefaultTask.class.getName())));

  }

}