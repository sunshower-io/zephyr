package io.sunshower.kernel.launch.validations;

import static io.sunshower.kernel.launch.KernelOptions.SystemProperties.SUNSHOWER_HOME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.test.common.Tests;
import java.io.File;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SystemPropertyFileValidationStepTest {

  private transient File file;
  private transient KernelOptions options;

  @BeforeEach
  void setUp() {
    file = Tests.createTemp();
    options = new KernelOptions();
  }

  @Test
  void ensurePropertyFileWorksWhenSystemPropertyExists() {
    System.setProperty(SUNSHOWER_HOME, file.getAbsolutePath());
    val n = new SystemPropertyFileValidationStep(SUNSHOWER_HOME);
    val validatable = mock(Validatable.class);
    n.validate(validatable, options);
    assertEquals(file, options.getHomeDirectory(), "sunshower home directory must be set");
  }

  @Test
  void ensurePropertyDoesNotChangeHomeDirectoryWhenHomeDirectoryIsSet() {
    val newFile = Tests.createTemp();
    System.setProperty(SUNSHOWER_HOME, file.getAbsolutePath());
    options.setHomeDirectory(newFile);
    val n = new SystemPropertyFileValidationStep(SUNSHOWER_HOME);
    val validatable = mock(Validatable.class);
    n.validate(validatable, options);
    assertEquals(
        options.getHomeDirectory(), newFile, "sunshower home directory must not be changed");
  }
}
