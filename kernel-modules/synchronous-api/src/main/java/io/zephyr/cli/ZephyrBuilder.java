package io.zephyr.cli;

import java.io.File;

public interface ZephyrBuilder {
  BuilderWithHomeDirectory homeDirectory(File file);
}
