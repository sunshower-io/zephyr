package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TasksTest {

  ReductionScope scope;
  Context context;

  @BeforeEach
  void setUp() {
    context = ReductionScope.newContext();
    scope = ReductionScope.newRoot(context);
  }

  @Test
  void ensureProcessBuilderWithSimpleTaskProducesCorrectGraph() {
    val task = mock(Task.class);
    val process = Tasks.newProcess("a").withContext(scope).register("test", task).create();
  }
}
