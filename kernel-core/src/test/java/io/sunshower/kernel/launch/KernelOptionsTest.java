package io.sunshower.kernel.launch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.sunshower.test.common.Tests;
import java.io.File;
import java.util.logging.*;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KernelOptionsTest {

  Logger logger = Logger.getLogger("SunshowerKernel");
  private File home;

  @BeforeEach
  void setUp() {
    val handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    logger.addHandler(handler);
    logger.setLevel(Level.ALL);

    home = Tests.createTemp();
  }

  @Test
  void ensureValidationWorksForSystemProperty() {
    System.setProperty(KernelOptions.SystemProperties.SUNSHOWER_HOME, home.getAbsolutePath());
    val options = new KernelOptions();
    options.validate();
    assertEquals(options.getHomeDirectory(), home);
  }
}
