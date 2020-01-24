package io.sunshower.gyre;

import static io.sunshower.gyre.DirectedGraph.incoming;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParallelSchedulerTest {

  String toSchedule =
      "des_system_lib std synopsys std_cell_lib dw02 dw01 ramlib ieee\n"
          + "dw01           ieee dware gtech\n"
          + "dw02           ieee dware\n"
          + "dw03           std synopsys dware dw02 dw01 ieee gtech\n"
          + "dw04           ieee dw01 dware gtech\n"
          + "dw05           ieee dware\n"
          + "dw06           ieee dware\n"
          + "dw07           ieee dware\n"
          + "dware          ieee\n"
          + "gtech          ieee\n"
          + "ramlib         std ieee\n"
          + "std_cell_lib   ieee\n"
          + "synopsys";

  Graph<DirectedGraph.Edge<String>, String> graph;
  ParallelScheduler<DirectedGraph.Edge<String>, String> scheduler;

  @BeforeEach
  void setUp() {
    graph = new AbstractDirectedGraph<>();
    scheduler = new ParallelScheduler<>();
  }

  @Test
  void ensureGraphPrintsCorrectly() {
    graph.connect("a", "b", incoming("a -> b"));
    graph.connect("b", "c", incoming("b -> c"));
    graph.connect("b", "d", incoming("b -> d"));
    val result = new GraphWriter<DirectedGraph.Edge<String>, String>().write(graph);
    assertNotNull(result, "must have a result");
  }

  @Test
  void testPlan() {
    parse(toSchedule);
    System.out.println(graph);
    val result = scheduler.apply(graph);
    assertEquals(result.size(), 4, "must have 4 elements");
  }

  @Test
  void ensureDependentsAreCorrect() {
    parse(toSchedule);
    val result = scheduler.apply(graph);
    val dware = find("dware", result);
    assertTrue(
        dware.getPredecessors().contains(find("ieee", result)),
        "must contain correct simple neighbor");
  }

  private void parse(String value) {
    String[] lines = value.split("\n");
    for (String line : lines) {
      String[] deps = line.split("\\s+");

      val source = deps[0];
      for (int i = 1; i < deps.length; i++) {
        val dep = deps[i];
        graph.connect(source, dep, incoming(format("%s <- %s", line, dep)));
      }
    }
  }

  Task<DirectedGraph.Edge<String>, String> find(
      String name, Schedule<DirectedGraph.Edge<String>, String> result) {
    for (val s : result) {
      for (val t : s.getTasks()) {
        if (t.getValue().equals(name)) {
          return t;
        }
      }
    }
    return null;
  }
}
