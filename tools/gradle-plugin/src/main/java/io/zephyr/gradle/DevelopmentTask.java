package io.zephyr.gradle;

import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class DevelopmentTask extends DefaultTask {

  @TaskAction
  public void perform() {
    val project = getProject();
    for (val cfg : project.getConfigurations()) {
      for(val artifact : cfg.getResolvedConfiguration().getResolvedArtifacts()) {
        System.out.println(artifact.getFile());
      }
      for (val dep : cfg.getDependencies()) {
        System.out.println(dep);
      }
    }
  }
}
