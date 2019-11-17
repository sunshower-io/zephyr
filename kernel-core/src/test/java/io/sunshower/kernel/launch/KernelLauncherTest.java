package io.sunshower.kernel.launch;

import io.sunshower.test.common.Tests;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class KernelLauncherTest {

  public static void main(String[] args) throws IOException {}

  @Test
  void ensureLauncherWorksWithNoArguments() throws IOException {
    KernelLauncher.main(new String[0]);
  }
}
