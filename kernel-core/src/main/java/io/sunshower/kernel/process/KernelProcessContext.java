package io.sunshower.kernel.process;

import io.sunshower.kernel.core.ContextValueNotFoundException;
import io.sunshower.kernel.core.Kernel;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class KernelProcessContext {

  @Getter private final Kernel kernel;
  private final Map<String, Object> context;

  public KernelProcessContext(Kernel kernel) {
    this.kernel = kernel;
    context = new HashMap<>();
  }

  public <T> void setContextValue(@NonNull String name, @NonNull T value) {
    context.put(name, value);
  }

  public <T> T getContextValue(@NonNull String name) {
    val value = context.get(name);
    if (value == null) {
      throw new ContextValueNotFoundException(name);
    }
    @SuppressWarnings("unchecked")
    val t = (T) value;
    return t;
  }
}
