package io.zephyr.cli;

import io.sunshower.test.common.Tests;
import lombok.val;
import org.junit.jupiter.api.Test;

public class ZephyrBuilderTest {

  @Test
  void ensureZephyrBuilderApiWorks() {
    Zephyr zephyr =
        Zephyr.builder()
            .homeDirectory(Tests.createTemp())
            .maxKernelThreads(2)
            .maxUserThreads(2)
            .create();

    zephyr.startup();
    zephyr.shutdown();
  }

  @Test
  void ensureZephyrBuilderApiWorksWithDefaults() {
    val zep = Zephyr.builder().homeDirectory(Tests.createTemp()).create();
    zep.startup();
    zep.shutdown();
  }
}
