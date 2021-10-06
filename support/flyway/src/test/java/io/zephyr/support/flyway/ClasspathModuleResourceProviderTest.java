package io.zephyr.support.flyway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.Assembly;
import io.zephyr.kernel.Module;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClasspathModuleResourceProviderTest {

  private Assembly assembly;
  @Mock
  private Module module;

  @Test
  void ensureResourcesAreLoadableFromWAR() {
    configureAssembly("test-plugins:test-plugin-1", "war");
    val results =
        FlywaySupport.classpath(module).locations("flyway").getResources("V", new String[]{".sql"});
    assertEquals(2, results.size());
  }

  @Test
  void ensureResourcesAreLoadableFromJAR() {
    configureAssembly("test-plugins:test-plugin-1", "jar");
    val results =
        FlywaySupport.classpath(module).locations("flyway").getResources("V", new String[]{".sql"});
    assertEquals(2, results.size());
  }

  @Test
  void ensureLoadingFromMultipleWARLocationsWorks() {
    configureAssembly("test-plugins:test-plugin-1", "war");
    val results =
        FlywaySupport.classpath(module)
            .locations("flyway", "location2")
            .getResources("V", new String[]{".sql"});
    assertEquals(3, results.size());
  }

  @Test
  void ensureLoadingFromMultipleJARLocationsWorks() {
    configureAssembly("test-plugins:test-plugin-1", "jar");
    val results =
        FlywaySupport.classpath(module)
            .locations("flyway", "location2")
            .getResources("V", new String[]{".sql"});
    assertEquals(3, results.size());
  }

  private void configureAssembly(String testPlugin, String ext) {
    val file = Tests.relativeToProjectBuild("kernel-tests:" + testPlugin, ext, "libs");
    if (!file.exists()) {
      throw new IllegalArgumentException("No such file: " + file.getAbsolutePath());
    }
    assembly = new Assembly(file);
    given(module.getAssembly()).willReturn(assembly);
  }
}
