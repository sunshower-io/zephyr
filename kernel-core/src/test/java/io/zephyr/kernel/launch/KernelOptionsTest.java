package io.zephyr.kernel.launch;

import io.zephyr.common.Options;
import io.zephyr.kernel.extensions.EntryPoint;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumMap;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

class KernelOptionsTest {

    @Test
    void ensureLogLevelSpecificityDefaultsToWarning() {
        val context = new EnumMap<>(EntryPoint.ContextEntries.class);
        context.put(EntryPoint.ContextEntries.ARGS, Collections.EMPTY_LIST);

        val options = Options.create(KernelOptions::new, context);

        assertEquals(Level.WARNING, options.getLogLevel());
    }

    @Test
    void ensureLogLevelIsSettable() {
        val context = new EnumMap<>(EntryPoint.ContextEntries.class);
        context.put(EntryPoint.ContextEntries.ARGS, Collections.singletonList("info"));

        val options = Options.create(KernelOptions::new, context);

        assertEquals(Level.INFO, options.getLogLevel());
    }

}