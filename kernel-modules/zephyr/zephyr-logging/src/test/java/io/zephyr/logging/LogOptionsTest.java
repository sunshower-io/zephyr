package io.zephyr.logging;

import static org.junit.jupiter.api.Assertions.*;

import io.zephyr.common.Options;
import io.zephyr.kernel.extensions.EntryPoint;
import java.util.EnumMap;
import lombok.val;
import org.junit.jupiter.api.Test;

class LogOptionsTest {

  @Test
  void ensureHomeDirectoryIsZephyrByDefault() {
    val context = new EnumMap<>(EntryPoint.ContextEntries.class);
    context.put(EntryPoint.ContextEntries.ARGS, new String[0]);

    val options = Options.create(LogOptions::new, context);

    assertEquals(
        "zephyr",
        options.getHomeDirectory().getName(),
        "Home directory should be ZEPHYR by default");
  }

  @Test
  void ensureHomeDirectoryCanBeSet() {
    val context = new EnumMap<>(EntryPoint.ContextEntries.class);
    context.put(EntryPoint.ContextEntries.ARGS, new String[] {"-h", "boop"});

    val options = Options.create(LogOptions::new, context);

    assertEquals("boop", options.getHomeDirectory().getName(), "Home directory should be settable");
  }
}
