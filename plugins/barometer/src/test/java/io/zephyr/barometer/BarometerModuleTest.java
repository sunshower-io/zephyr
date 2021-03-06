package io.zephyr.barometer;

import io.sunshower.kernel.test.Clean;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleContext;
import io.zephyr.kernel.core.Kernel;
import javax.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Clean
@ZephyrTest
public class BarometerModuleTest {

  @Inject private Kernel kernel;

  @Inject private ModuleContext context;

  private BarometerModule module;

  @BeforeEach
  void setUp() {
    module = new BarometerModule();
    val path = kernel.getFileSystem().getPath("barometer/zephyr.pid");
    if (path.toFile().exists()) {
      path.toFile().delete();
    }
  }

  @Test
  void ensureStartupIsCorrect() {

    module.start(context);
    module.stop(context);
  }
}
