package io.zephyr.kernel.core;

import io.zephyr.api.ModuleContext;
import io.zephyr.api.PluginActivator;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.TaskQueue;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractModule implements Module {
  @Getter @Setter protected ModuleContext context;
  @Getter @Setter protected ModuleLoader moduleLoader;

  public abstract void setActivator(PluginActivator o);

  public abstract void setTaskQueue(TaskQueue thread);
}
