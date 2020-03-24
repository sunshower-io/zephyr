package io.zephyr.gradle;

import io.zephyr.cli.Zephyr;
import java.util.concurrent.atomic.AtomicReference;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class DevelopmentPlugin implements Plugin<Project> {
  static final String[] configurations = {
    "zephyrModule" // creates the default module configuration
    ,
    "zephyrDevelopmentModule" // creates the default development modules configuration
    ,
    "zephyrKernelModule" // creates the default kernel modules configuration
  };

  static final AtomicReference<Zephyr> instance;
  static final AtomicReference<InstanceList> instanceList;

  static {
    instance = new AtomicReference<>();
    instanceList = new AtomicReference<>();
  }

  public static InstanceList getInstanceList() {
    val result = instanceList.get();
    if (result == null) {
      throw new IllegalStateException("Error: instance list has not been created yet.");
    }

    return result;
  }

  public static void setInstanceList(InstanceList list) {
    instanceList.set(list);
  }

  public static void setInstance(Zephyr zephyr) {
    instance.set(zephyr);
  }

  public static Zephyr getInstance() {
    val result = instance.get();
    if (result == null) {
      throw new IllegalStateException("Attempting to retrieve zephyr when no task has started one");
    }
    return result;
  }

  public static boolean isPluginConfiguration(String cfg) {
    for (val pluginConfiguration : configurations) {
      if (pluginConfiguration.equals(cfg)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void apply(Project project) {
    createConfiguration(project);
    registerTasks(project);

    val plugins = project.getPlugins();
    plugins.withType(JavaPlugin.class, javaPlugin -> {});
  }

  private void registerTasks(Project project) {
    val tasks = project.getTasks();
    val devTask = tasks.create("zephyrDev", DevelopmentTask.class);
    tasks.create("zephyrStop", ShutdownTask.class);
    tasks.create("zephyrConnect", ZephyrConnectTask.class);
    devTask.finalizedBy("zephyrStop");
  }

  private void createConfiguration(Project project) {
    val configurationContainer = project.getConfigurations();
    for (val configuration : configurations) {
      configurationContainer.create(configuration);
    }
  }
}
