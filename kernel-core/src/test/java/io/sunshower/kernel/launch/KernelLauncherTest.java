package io.sunshower.kernel.launch;

import io.sunshower.test.common.Tests;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;


class KernelLauncherTest {

  public static void main(String[] args) throws IOException {}

  @Test
  void testLauncher() throws IOException {

    val temp = Tests.createTemp();
    KernelLauncher.main(new String[] {"-h", temp.getAbsolutePath(), "-i"});
  }
}
