package io.sunshower.kernel.lifecycle.processes;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.concurrency.TaskGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KernelStartProcessTest {

  private TaskGraph<String> graph;

  @BeforeEach
  void setUp() {
    graph = new TaskGraph<String>();
  }

  @Test
  void ensureStartProcessWorks() {}
}
