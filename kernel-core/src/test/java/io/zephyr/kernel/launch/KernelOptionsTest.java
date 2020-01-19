package io.zephyr.kernel.launch;

import static org.junit.jupiter.api.Assertions.*;

import io.zephyr.common.Options;
import io.zephyr.kernel.extensions.EntryPoint;
import java.util.EnumMap;
import java.util.logging.Level;
import lombok.val;
import org.junit.jupiter.api.Test;

class KernelOptionsTest {

  @Test
  void ensureLogLevelSpecificityDefaultsToWarning() {
    val context = new EnumMap<>(EntryPoint.ContextEntries.class);
    context.put(EntryPoint.ContextEntries.ARGS, new String[0]);

    val options = Options.create(KernelOptions::new, context);

    assertEquals(Level.WARNING, options.getLogLevel());
  }

  @Test
  void ensureLogLevelIsSettable() {
    val context = new EnumMap<>(EntryPoint.ContextEntries.class);
    context.put(EntryPoint.ContextEntries.ARGS, new String[]{"-l", "info"});

    val options = Options.create(KernelOptions::new, context);

    assertEquals(Level.INFO, options.getLogLevel());
  }
}
