package io.zephyr.gradle;

import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DevelopmentPlugin implements Plugin<Project> {
  static final String[] configurations = {
    "zephyrModule" // creates the default module configuration
    ,
    "zephyrDevelopmentModule" // creates the default development modules configuration
    ,
    "zephyrKernelModule" // creates the default kernel modules configuration
  };

  @Override
  public void apply(Project project) {
    createConfiguration(project);
    registerTasks(project);
  }

  private void registerTasks(Project project) {
    project.getTasks().create("zephyrDev", DevelopmentTask.class);
  }

  private void createConfiguration(Project project) {
    val configurationContainer = project.getConfigurations();
    for (val configuration : configurations) {
      configurationContainer.create(configuration);
    }
  }
}
