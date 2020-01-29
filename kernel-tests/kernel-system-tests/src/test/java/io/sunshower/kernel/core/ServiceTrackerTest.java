package io.sunshower.kernel.core;

import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleContext;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@ZephyrTest
public class ServiceTrackerTest {

  @Inject
  private ModuleContext context;

  @Test
  void ensureTrackingServiceProducesServiceInInstalledTestPlugin() {
  }
}
