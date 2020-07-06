package io.zephyr.bundle.sfx;

/** */
public interface ExecutableFileIconService extends PlatformSpecificService {
  void setIcons(SelfExtractingExecutableConfiguration configuration, Log log);
}
