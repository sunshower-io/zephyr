package io.sunshower.gyre;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.sunshower.gyre.DirectedGraph.outgoing;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

class SerialSchedulerTest {
  Graph<DirectedGraph.Edge<String>, String> graph;
  private Transformation<
          DirectedGraph.Edge<String>, String, Schedule<DirectedGraph.Edge<String>, String>>
      scheduler;

  @BeforeEach
  void setUp() {
    graph = new AbstractDirectedGraph<>();
    scheduler = new SerialScheduler<>();
  }

  @Test
  void ensureComplexTaskIsScheduledCorrectly() {
    graph.connect("5", "11", outgoing("5 -> 11"));
    graph.connect("11", "2", outgoing("11 -> 2"));
    graph.connect("7", "11", outgoing("7 -> 11"));
    graph.connect("7", "8", outgoing("7 -> 8"));
    graph.connect("3", "8", outgoing("3 -> 8"));
    graph.connect("3", "10", outgoing("3 -> 10"));
    graph.connect("11", "9", outgoing("11 -> 9"));
    graph.connect("8", "9", outgoing("8 -> 9"));
    graph.connect("11", "10", outgoing("11 -> 10"));

    val schedule = scheduler.apply(graph);

    assertEquals(schedule.getTasks().size(), graph.vertexCount());
  }

  @Test
  void ensureAllElementsArePresentInSchedule() {

    c("1", "0");
    c("2", "1");
    c("0", "3");
    c("3", "4");
    val g = scheduler.apply(graph);
    assertEquals(g.size(), 5);
  }

  private void c(String source, String target) {
    graph.connect(source, target, outgoing(format("%s -> %s", source, target)));
  }
}
