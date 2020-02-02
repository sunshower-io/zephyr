package io.zephyr.gradle;

import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class DevelopmentTask extends DefaultTask {

  @TaskAction
  public void perform() {
    val project = getProject();

//    val kernel = createKernel();
    for (val cfg : project.getConfigurations()) {
      for (val artifact : cfg.getResolvedConfiguration().getResolvedArtifacts()) {


      }
    }
  }


}
