package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sunshower.gyre.Scope;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitAssertionsShouldIncludeMessage",
  "PMD.JUnitTestContainsTooManyAsserts"
})
class KernelSchedulerTest {

  private KernelScheduler<String> scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new KernelScheduler<>(new ExecutorWorkerPool(Executors.newFixedThreadPool(1)));
  }

  @Test
  void ensureTaskOrderIsCorrect() throws ExecutionException, InterruptedException {
    val proc =
        Tasks.newProcess("test")
            .register(
                new Task("a") {
                  @Override
                  public TaskValue run(Scope scope) {
                    return null;
                  }
                })
            .register(
                new Task("b0") {

                  @Override
                  public TaskValue run(Scope scope) {
                    scope.set("t", "b0");
                    return null;
                  }
                })
            .register(
                new Task("b1") {

                  @Override
                  public TaskValue run(Scope scope) {
                    scope.set("t", "b1");
                    return null;
                  }
                })
            .register(
                new Task("c0") {
                  @Override
                  public TaskValue run(Scope scope) {
                    assertEquals(scope.get("t"), "b0");
                    return null;
                  }
                })
            .register(
                new Task("c1") {

                  @Override
                  public TaskValue run(Scope scope) {
                    assertEquals(scope.get("t"), "b1");
                    return null;
                  }
                })
            .task("b0")
            .dependsOn("a")
            .task("b1")
            .dependsOn("a")
            .task("c1")
            .dependsOn("b1")
            .task("c0")
            .dependsOn("b0")
            .create();
    scheduler.submit(proc).get();
  }
}
