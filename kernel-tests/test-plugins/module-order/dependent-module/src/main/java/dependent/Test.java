package dependent;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import lombok.val;

public class Test implements ModuleActivator {

  public Test() {}

  @Override
  public void start(ModuleContext context) {
    boolean found = false;
    try {
      val type = Class.forName("one.Test", false, Thread.currentThread().getContextClassLoader());
      type.getDeclaredMethod("v1");
      found = true;
    } catch (Exception ex) {

    }

    if (!found) {
      throw new IllegalStateException("Not found!");
    }
  }

  @Override
  public void stop(ModuleContext context) {}
}
