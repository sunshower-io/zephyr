package io.sunshower.kernel.lifecycle.processes;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.concurrency.Context;
import io.sunshower.kernel.concurrency.ReductionScope;
import io.sunshower.kernel.concurrency.TaskGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KernelFilesystemCreatePhaseTest {

  private Context context;
  private ReductionScope scope;
  private TaskGraph<String> graph;

  @BeforeEach
  void setUp() {
    context = ReductionScope.newContext();
    scope = ReductionScope.newRoot(context);
    graph = new TaskGraph<>();
  }

  @Test
  void ensureCreatePhaseProducesFileSystem() {
    graph.add(new KernelFilesystemCreatePhase());
    //    scheduler.submit(
    //        Tasks.newProcess("kernel:start")
    //            .autoparallelize()
    //            .coalesce()
    //            .withScope(scope)
    //            .register("kernel:lifecycle:filesystem:create", new KernelFilesystemCreatePhase())
    //            .create());
  }
}
