package io.zephyr.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

public class DevelopmentPluginTest {

  @Test
  void ensureDevelopmentPluginProvidesModulesConfiguration() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("io.zephyr.gradle");
    val cfg = project.getConfigurations().getByName("zephyrModule");

    assertEquals("zephyrModule", cfg.getName(), "must have correct name");
    assertEquals(0, cfg.getDependencies().size(), "must have no dependencies");
  }

  @Test
  void ensureDevelopmentPluginProvidesDevModulesConfiguration() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("io.zephyr.gradle");
    val cfg = project.getConfigurations().getByName("zephyrDevelopmentModule");

    assertEquals("zephyrDevelopmentModule", cfg.getName(), "must have correct name");
    assertEquals(0, cfg.getDependencies().size(), "must have no dependencies");
  }

  @Test
  void ensureDevelopmentPluginSuppliesDependenciesWhenAdded() {

    final BuildResult build =
        GradleRunner.create()
            .withProjectDir(
                resources("test-projects/configuration-tests/module-configurations-with-deps"))
            .withPluginClasspath()
            .withDebug(true)
            .withArguments("zephyrDev")
            .build();
    System.out.println(build.getOutput());

    assertEquals(TaskOutcome.SUCCESS, build.task(":zephyrDev").getOutcome(), "must be successful");
  }

  File resources(String path) {
    val url = ClassLoader.getSystemClassLoader().getResource(path);
    val result = new File(url.getFile());
    if (!result.exists()) {
      throw new IllegalStateException("No build.gradle found in " + result.getAbsolutePath());
    }
    return result;
  }
}
