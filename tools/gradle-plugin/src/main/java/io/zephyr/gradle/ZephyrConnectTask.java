package io.zephyr.gradle;

import java.io.File;
import java.io.IOException;
import lombok.Synchronized;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ZephyrConnectTask extends DefaultTask {

  @TaskAction
  public void perform() throws IOException {
    val instanceList = getOrCreateInstanceList();
    val instance = instanceList.connect();
    instance.connect();
  }

  @Synchronized
  private InstanceList getOrCreateInstanceList() {
    var result = DevelopmentPlugin.instanceList.get();
    if (result == null) {
      result = new InstanceList(new File(getProject().getBuildDir(), "zephyr_tmp"));
      DevelopmentPlugin.setInstanceList(result);
    }
    return result;
  }
}
