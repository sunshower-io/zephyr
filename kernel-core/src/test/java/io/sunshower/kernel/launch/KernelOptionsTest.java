package io.sunshower.kernel.launch;


import io.sunshower.test.common.Tests;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class KernelOptionsTest {

    private KernelOptions options;

    @BeforeEach
    void setUp() {
        options = new KernelOptions();
    }

    @Test
    void ensureHelpMapsCorrectly() {
        assertTrue(options.satisfy("-h").isHelp());
    }

    @Test
    void ensurePropertyFileIsResolvedCorrectly() {
        System.setProperty("sunshower.home", Tests.buildDirectory().getAbsolutePath());
        val a = options.satisfy("-h");
        assertNotNull(a.storage);
    }

}
