package io.zephyr.bundle.sfx;

import java.io.File;

public interface SelfExecutingBundler extends PlatformSpecificService {

  File load(File workspaceDirectory, Log log);

  void create(BundleOptions options, Log log);
}
