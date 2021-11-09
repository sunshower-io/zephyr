package one;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;

public class Test implements ModuleActivator {

  public void v1() {}

  @Override
  public void start(ModuleContext context) {
    System.out.println("Module 1 V1 starting");
  }

  @Override
  public void stop(ModuleContext context) {
    System.out.println("Module 1 V1 stopping");
  }
}
