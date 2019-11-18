package io.zephyr.kernel.launch;

import static org.junit.jupiter.api.Assertions.*;

import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import io.zephyr.kernel.shell.Color;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressFBWarnings
@SuppressWarnings("PMD.UseProperClassLoader")
class BannerTest {

  Banner banner;

  @BeforeEach
  void setUp() {
    banner = new Banner();
  }

  @Test
  void ensureBannerIsReadable() {
    assertNotNull(
        KernelLauncher.class.getClassLoader().getSystemResource("assets/banner.txt"),
        "must have correct asset");
  }

  @Test
  void ensureScanningManifestWorks() throws IOException {
    val result =
        " _____              __\n"
            + "/__  /  ___  ____  / /_  __  _______\n"
            + "  / /  / _ \\/ __ \\/ __ \\/ / / / ___/\n"
            + " / /__/  __/ /_/ / / / / /_/ / /\n"
            + "/____/\\___/ .___/_/ /_/\\__, /_/\n"
            + "         /_/          /____/\n";

    val bytes = new ByteArrayOutputStream();
    val writer = new PrintStream(bytes);
    banner.print(writer);
    assertTrue(bytes.toString().contains(result), "expected must be contained in actual");
  }
}
