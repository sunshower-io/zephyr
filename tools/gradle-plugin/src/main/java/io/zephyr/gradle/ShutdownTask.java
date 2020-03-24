package io.zephyr.gradle;

import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.TaskAction;

public class ShutdownTask extends DefaultTask {

  @TaskAction
  public void perform() {
    val logger = getLogger();
    logger.log(LogLevel.ERROR, "Shutting down running Zephyr...");
    DevelopmentPlugin.getInstance().shutdown();
    logger.log(LogLevel.ERROR, "Zephyr successfully shutdown");
  }
}
