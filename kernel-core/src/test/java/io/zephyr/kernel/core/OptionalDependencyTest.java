package io.zephyr.kernel.core;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.zephyr.kernel.Lifecycle.State;
import java.util.concurrent.TimeUnit;
import lombok.val;
import org.junit.jupiter.api.Test;

public class OptionalDependencyTest extends ModuleManagerTestCase {

  protected String getPlugin2() {
    return "kernel-tests:test-plugins:optional:test-plugin-2";
  }

  protected String getPlugin1() {
    return "kernel-tests:test-plugins:optional:test-plugin-1";
  }

  @Test
  void ensureInstallingTestPluginWithOptionalDependencyWorks() {
    install(plugin2);
    val plugin = find("optional-test-plugin-2");
    assertNotNull(plugin);
    start("optional-test-plugin-2");
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> kernel.getModuleManager().getModules(State.Active).size() == 1);
    assertEquals(1, kernel.getModuleManager().getModules(State.Active).size());
  }

  @Test
  void ensureInstallingTestPluginWithOptionalDependencyPresentWorks() {
    install(plugin2, plugin1);
    val plugin = find("optional-test-plugin-2");
    assertNotNull(plugin);
    start("optional-test-plugin-2");
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> kernel.getModuleManager().getModules(State.Active).size() == 2);
  }
}
