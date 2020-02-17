package io.zephyr.cli;

public interface Creator {

  Zephyr create(ClassLoader classLoader);

  default Zephyr create() {
    return create(Thread.currentThread().getContextClassLoader());
  }
}
