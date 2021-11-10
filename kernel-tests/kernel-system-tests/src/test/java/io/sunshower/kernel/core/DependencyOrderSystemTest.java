package io.sunshower.kernel.core;

import io.sunshower.kernel.test.Clean;
import io.sunshower.kernel.test.Module;
import io.sunshower.kernel.test.Modules;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.core.Kernel;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@ZephyrTest
@Modules({
  @Module(project = "kernel-tests:test-plugins:module-order:module-one-v1"),
  @Module(project = "kernel-tests:test-plugins:module-order:module-one-v2"),
  @Module(project = "kernel-tests:test-plugins:module-order:dependent-module"),
})
@DisabledOnOs(OS.WINDOWS)
@Clean(value = Clean.Mode.Before, context = Clean.Context.Method)
public class DependencyOrderSystemTest {

  @Inject private Kernel kernel;

  @Inject private Zephyr zephyr;

  @Test
  void ensureModuleStartOrderWorks() throws InterruptedException {
    zephyr.start("sunshower.io:dependent-module:1.0.0-SNAPSHOT");
    //    assertEquals(3, zephyr.getPluginCoordinates(State.Active).size());
    zephyr.stop(List.of("sunshower.io:dependent-module:1.0.0-SNAPSHOT"));
  }
}
