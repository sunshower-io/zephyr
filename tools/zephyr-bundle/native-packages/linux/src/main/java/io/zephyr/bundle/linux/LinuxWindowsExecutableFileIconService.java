package io.zephyr.bundle.linux;

import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.ExecutableFileIconService;
import io.zephyr.bundle.sfx.Log;

import java.io.File;
import java.util.Collection;

public class LinuxWindowsExecutableFileIconService implements ExecutableFileIconService {

  @Override
  public void setIcons(File executable, File icoFile, Log log) {}

  @Override
  public void setIcons(File executable, Collection<File> iconFiles, Log log) {}

  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return false;
  }
}
