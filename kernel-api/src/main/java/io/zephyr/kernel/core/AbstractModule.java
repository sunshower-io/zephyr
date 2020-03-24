package io.zephyr.kernel.core;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.TaskQueue;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractModule implements Module {
  @Getter @Setter protected ModuleContext context;
  @Getter @Setter protected ModuleLoader moduleLoader;

  public abstract void setModuleClasspath(ModuleClasspath classpath);

  public abstract void setActivator(ModuleActivator o);

  public abstract void setTaskQueue(TaskQueue thread);
}
