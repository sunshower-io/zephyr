package io.sunshower.kernel.core;

import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleContext;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@ZephyrTest
public class ServiceTrackerTest {

  @Inject private ModuleContext context;

  @Test
  void ensureTrackingServiceProducesServiceInInstalledTestPlugin() {}
}
