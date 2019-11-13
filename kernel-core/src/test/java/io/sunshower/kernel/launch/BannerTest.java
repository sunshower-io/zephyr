package io.sunshower.kernel.launch;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

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
        "Welcome to\n"
            + "======================================\n"
            + " _____\n"
            + "/__  /  ___  ____  __  _______\n"
            + "  / /  / _ \\/ __ \\/ / / / ___/\n"
            + " / /__/  __/ /_/ / /_/ / /\n"
            + "/____/\\___/ .___/\\__, /_/\n"
            + "         /_/    /____/\n"
            + "\n"
            + "======================================\n"
            + "ZepyrCore :: Kernel Version 2.0.0-SNAPSHOT :: Revision 11.0.1 (Oracle Corporation 11.0.1+13)\n"
            + "Build: c1be6c4a7d08ba72e10383630c482bf307d6d9b5";

    val bytes = new ByteArrayOutputStream();
    val writer = new PrintStream(bytes);
    banner.print(writer);
    assertEquals(bytes.toString().trim(), result, "must be same string");
  }
}
