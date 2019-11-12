package io.sunshower.gyre;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReverseSubgraphTransformationTest {
  private AbstractDirectedGraph<String, String> graph;

  @BeforeEach
  void setUp() {
    graph = new AbstractDirectedGraph<String, String>();
  }

  @Test
  void ensureTrivialDependentClosureIsCorrect() {

    graph.add("a");
    val schedule = scheduleFrom("a");
    assertSchedule(schedule, level("a"));
  }

  @Test
  void ensureSimpleScenarioWorks() {
    connect("plugin2", "plugin1");

    var s = scheduleFrom("plugin2");
    assertSchedule(s, level("plugin2"));
  }

  @Test
  void ensureLargeScenarioIsCorrect() {
    connect("a", "b");
    connect("b", "c");
    connect("b", "d");
    connect("d", "f");
    connect("d", "e");
    connect("f", "x");
    connect("e", "x");

    var schedule = scheduleFrom("a");
    assertSchedule(schedule, level("a"));
    schedule = scheduleFrom("b");
    assertSchedule(schedule, level("a"), level("b"));

    schedule = scheduleFrom("d");
    assertSchedule(schedule, level("a"), level("b"), level("d"));
    schedule = scheduleFrom("x");
    System.out.println(schedule);
  }

  private void connect(String a, String b) {
    graph.connect(a, b, DirectedGraph.incoming(String.format("%s dependsOn %s", a, b)));
  }

  private void assertSchedule(
      Schedule<DirectedGraph.Edge<String>, String> s,
      TaskSet<DirectedGraph.Edge<String>, String>... sets) {
    assertEquals(s.size(), sets.length, "must have same number of tasks");
    for (int i = 0; i < sets.length; i++) {
      val tset = s.get(i);
      val sset = sets[i];
      assertEquals(tset.size(), sset.size(), "level set must have same size");
      val titer = tset.getTasks().iterator();
      val siter = sset.getTasks().iterator();
      while (titer.hasNext()) {
        assertEquals(titer.next().getValue(), siter.next().getValue());
      }
    }
  }

  TaskSet<DirectedGraph.Edge<String>, String> level(String... tasks) {
    val ts = new MutableTaskSet<DirectedGraph.Edge<String>, String>();
    for (val t : tasks) {
      ts.tasks.add(
          new Task<>() {
            @Override
            public String getValue() {
              return t;
            }

            @Override
            public Scope getScope() {
              return null;
            }

            @Override
            public Set<Task<DirectedGraph.Edge<String>, String>> getPredecessors() {
              return null;
            }

            @Override
            public Set<DirectedGraph.Edge<String>> getEdges() {
              return null;
            }
          });
    }
    return ts;
  }

  private Schedule<DirectedGraph.Edge<String>, String> scheduleFrom(String a) {
    return new ParallelScheduler<DirectedGraph.Edge<String>, String>()
        .apply(t(a).apply(graph))
        .reverse();
  }

  Transformation<DirectedGraph.Edge<String>, String, Graph<DirectedGraph.Edge<String>, String>> t(
      String v) {
    return new ReverseSubgraphTransformation<>(v);
  }
}
