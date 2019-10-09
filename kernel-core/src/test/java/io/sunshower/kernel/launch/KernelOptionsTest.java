package io.sunshower.kernel.launch;


import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

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

}
