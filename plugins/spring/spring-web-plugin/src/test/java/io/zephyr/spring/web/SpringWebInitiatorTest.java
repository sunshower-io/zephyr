package io.zephyr.spring.web;

import io.sunshower.kernel.test.Module;
import io.sunshower.kernel.test.Modules;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.cli.Zephyr;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@DisabledOnOs(OS.WINDOWS)
@ZephyrTest
@Modules({
  @Module(project = "plugins:spring:spring-web-plugin"),
  @Module(project = "kernel-modules:sunshower-yaml-reader", type = Module.Type.KernelModule)
})
class SpringWebInitiatorTest {
  @Inject private Zephyr zephyr;

  @Test
  void ensureEverythingIsStarted() {
    zephyr.start("io.zephyr.spring:spring-web-plugin:1.0.0");
  }
}
