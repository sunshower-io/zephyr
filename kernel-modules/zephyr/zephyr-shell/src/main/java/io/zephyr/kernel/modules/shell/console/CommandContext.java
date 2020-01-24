package io.zephyr.kernel.modules.shell.console;

import io.zephyr.kernel.extensions.EntryPoint;
import java.util.Map;

public interface CommandContext {

  <T> T getService(Class<T> service);

  Map<EntryPoint.ContextEntries, Object> getLaunchContext();
}
