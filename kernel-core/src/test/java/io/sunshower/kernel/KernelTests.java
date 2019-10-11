package io.sunshower.kernel;

import static io.sunshower.test.common.Tests.projectOutput;
import static java.lang.String.format;

import java.io.File;
import java.net.URL;
import lombok.SneakyThrows;

public class KernelTests {

  @SneakyThrows
  public static URL loadTestModule(String module, String ext) {
    return loadTestModuleFile(module, ext).toURI().toURL();
  }

  public static File loadTestModuleFile(String module, String ext) {
    return projectOutput(format("kernel-modules:%s", module), ext);
  }

  @SneakyThrows
  public static URL loadTestPlugin(String name, String ext) {
    return loadTestPluginFile(name, ext).toURI().toURL();
  }

  @SneakyThrows
  public static File loadTestPluginFile(String name, String ext) {
    return projectOutput(format("kernel-tests:test-plugins:%s", name), ext);
  }
}
