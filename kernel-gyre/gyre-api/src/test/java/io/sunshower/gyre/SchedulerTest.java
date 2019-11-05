package io.sunshower.gyre;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static io.sunshower.gyre.DirectedGraph.outgoing;
import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

  Graph<DirectedGraph.Edge<String>, String> graph;
  private ParallelScheduler<DirectedGraph.Edge<String>, String> scheduler;

  @BeforeEach
  void setUp() {
    graph = new AbstractDirectedGraph<>();
    scheduler = new ParallelScheduler<>();
  }

  @Test
  void ensureSimpleParallelScheduleWorksWithFilter() {
    graph.connect("a", "b", outgoing("a -> b"));
    val schedule =
        scheduler.apply(graph, EdgeFilters.directionFilter(DirectedGraph.Direction.Outgoing));
    assertEquals(schedule.size(), 2, "parallel schedule must have 2 elements");
  }

  /** A -> B where "->" means "depends on" (so, B must be executed first) */
  @Test
  void ensureParallelScheduleHasCorrectElementsForSimpleCaseWithOutgoing() {
    graph.connect("a", "b", outgoing("a -> b"));
    val schedule = scheduler.apply(graph, EdgeFilters.outgoing());
    val fst = schedule.get(0).getTasks();
    assertEquals(fst.size(), 1, "must have single task in first level");
    assertEquals(fst.get(0).getValue(), "b");
  }

  @Test
  void ensureSimpleParallelScheduleWorks() {
    graph.connect("a", "b", outgoing("a -> b"));
    val schedule = scheduler.apply(graph);
    assertEquals(schedule.size(), 2, "parallel schedule must have 2 elements");
  }

  /** A -> B where "->" means "depends on" (so, B must be executed first) */
  @Test
  void ensureParallelScheduleHasCorrectElementsForSimpleCase() {
    graph.connect("a", "b", outgoing("a -> b"));
    val schedule = scheduler.apply(graph);
    val fst = schedule.get(0).getTasks();
    assertEquals(fst.size(), 1, "must have single task in first level");
    assertEquals(fst.get(0).getValue(), "b");
  }

  @Test
  @RepeatedTest(10)
  void ensureParallelScheduleWorksForComplexDependencyGraph() {
    graph.connect("5", "11", outgoing("5 -> 11"));
    graph.connect("11", "2", outgoing("11 -> 2"));
    graph.connect("7", "11", outgoing("7 -> 11"));
    graph.connect("7", "8", outgoing("7 -> 8"));
    graph.connect("3", "8", outgoing("3 -> 8"));
    graph.connect("3", "10", outgoing("3 -> 10"));
    graph.connect("11", "9", outgoing("11 -> 9"));
    graph.connect("8", "9", outgoing("8 -> 9"));
    graph.connect("11", "10", outgoing("11 -> 10"));
    val schedule =
        scheduler.apply(
            new TransitiveReduction<DirectedGraph.Edge<String>, String>().apply(graph),
            EdgeFilters.directionFilter(DirectedGraph.Direction.Outgoing));
    assertEquals(schedule.size(), 3, "must have 3 levels");

    expect(schedule.get(0), "2", "9", "10");
    expect(schedule.get(1), "11", "5", "8");
    expect(schedule.get(2), "3", "7");
  }

  private void expect(TaskSet<DirectedGraph.Edge<String>, String> stringTaskSet, String... labels) {
    assertEquals(
        stringTaskSet.getTasks().size(), labels.length, "size must be equal to expected size");
    val iter = stringTaskSet.getTasks().iterator();
    for (String label : labels) {
      assertEquals(label, iter.next().getValue(), "must have expected label");
    }
  }
}
