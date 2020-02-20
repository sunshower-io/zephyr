package io.zephyr.kernel.core;

import java.util.concurrent.atomic.AtomicReference;
import lombok.val;

@SuppressWarnings("PMD.SingletonClassReturningNewInstance")
public class Framework {

  private static final AtomicReference<Kernel> instance;

  static {
    instance = new AtomicReference<>();
  }

  public static void setInstance(Kernel instance) {
    Framework.instance.set(instance);
  }

  public static Kernel getInstance() {
    val inst = instance.get();
    if (inst == null) {
      throw new IllegalStateException("Instance must not be null");
    }
    return inst;
  }
}
