package io.zephyr.kernel;

import java.io.File;
import java.net.URI;

public interface Source {
  URI getLocation();

  boolean is(File file);
}
