package io.sunshower.kernel.concurrency;

import org.junit.jupiter.api.Test;

class KernelSchedulerTest {

  @Test
  void ensureTaskOrderIsCorrect() {
    Tasks.newProcess("test")
        .register(
            new Task("a") {
              @Override
              public TaskValue run(Context context) {
                return null;
              }
            })
        .register(
            new Task("b0") {

              @Override
              public TaskValue run(Context context) {
                return null;
              }
            })
        .register(
            new Task("b1") {

              @Override
              public TaskValue run(Context context) {
                return null;
              }
            })
        .register(
            new Task("c0") {
              @Override
              public TaskValue run(Context context) {
                return null;
              }
            })
        .register(
            new Task("c1") {

              @Override
              public TaskValue run(Context context) {
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
  }
}
