package io.zephyr.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.launch.KernelOptions;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogEntryPointTest {

  KernelOptions options;
  KernelLauncher launcher;
  LogEntryPoint logEntryPoint;
  Map<EntryPoint.ContextEntries, Object> entryPoints;

  @BeforeEach
  void setUp() {
    options = new KernelOptions();
    options.setHomeDirectory(Tests.createTemp());
    launcher = spy(KernelLauncher.class);
    given(launcher.getOptions()).willReturn(options);
    entryPoints = new EnumMap<>(EntryPoint.ContextEntries.class);
    entryPoints.put(EntryPoint.ContextEntries.ENTRY_POINTS, Collections.singletonList(launcher));
    logEntryPoint = new LogEntryPoint();
  }

  @Test
  void ensureLogFormatterWorks() {
    logEntryPoint.initialize(entryPoints);
    val root = logEntryPoint.getRootLogger();
    assertEquals(1, root.getHandlers().length, "must have 1 handler");
  }

  @Test
  void ensureLoggingIsOutputToCorrectDirectory() {
    logEntryPoint.initialize(entryPoints);
    val root = logEntryPoint.getRootLogger();
    root.log(Level.INFO, "sup wab");
  }
}
