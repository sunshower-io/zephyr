package io.sunshower.kernel.core;

import io.sunshower.test.common.Tests;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.net.URL;

public interface Installable {
  String getPath();

  default String getAssembly() {
    return getFile().getAbsolutePath();
  }

  @SneakyThrows
  default URL getUrl() {
    return getUri().toURL();
  }

  default URI getUri() {
    return getFile().toURI();
  }

  default File getFile() {
    return Tests.relativeToProjectBuild(getPath(), "war", "libs");
  }
}
