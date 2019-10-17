package io.sunshower.kernel.launch.validations;

import static io.sunshower.kernel.launch.KernelOptions.EnvironmentVariables.SUNSHOWER_HOME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class SystemEnvironmentVariableValidationStepTest {
  private transient File file;
  private transient KernelOptions options;
  private transient Map<String, String> environment;

  @BeforeEach
  void setUp() {
    file = Tests.createTemp();
    environment = new HashMap<>();
    options = new KernelOptions();
  }

  @Test
  void ensurePropertyFileWorksWhenSystemPropertyExists() {
    environment.put(SUNSHOWER_HOME, file.getAbsolutePath());
    val n = new SystemEnvironmentVariableValidationStep(SUNSHOWER_HOME, environment);
    val validatable = mock(Validatable.class);
    n.validate(validatable, options);
    assertEquals(file, options.getHomeDirectory(), "home directory must not change");
  }

  @Test
  void ensurePropertyDoesNotChangeHomeDirectoryWhenHomeDirectoryIsSet() {
    val newFile = Tests.createTemp();
    System.setProperty(SUNSHOWER_HOME, file.getAbsolutePath());
    options.setHomeDirectory(newFile);
    val n = new SystemEnvironmentVariableValidationStep(SUNSHOWER_HOME, environment);
    val validatable = mock(Validatable.class);
    n.validate(validatable, options);
    assertEquals(options.getHomeDirectory(), newFile, "home directory must not change");
  }
}
