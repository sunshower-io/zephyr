package io.zephyr.scan;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.extensions.EntryPoint;
import java.util.logging.Logger;

public class MockEntryPoint implements EntryPoint {
  final Kernel kernel;

  public MockEntryPoint(Kernel kernel) {
    this.kernel = kernel;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> type) {
    return (T) kernel;
  }

  @Override
  public <T> boolean exports(Class<T> type) {
    return Kernel.class.isAssignableFrom(type);
  }

  @Override
  public Logger getLogger() {
    return Logger.getAnonymousLogger();
  }

  @Override
  public Options<?> getOptions() {
    return null;
  }

  @Override
  public int getPriority() {
    return 0;
  }
}
