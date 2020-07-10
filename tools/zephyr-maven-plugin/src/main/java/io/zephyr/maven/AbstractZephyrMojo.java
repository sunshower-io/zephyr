package io.zephyr.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public abstract class AbstractZephyrMojo extends AbstractMojo {
  /**
   * since the actual file-name is platform-dependent, this property constitutes the base path of
   * the archive (such as the "target/aire" component of the path "target/aire.exe")
   */
  @Getter
  @Setter
  @Parameter(name = "archive-base", alias = "archive-base", property = "generate-sfx.archive-base")
  protected File archiveBase;

  /** which platform is this configuration targeting? */
  @Getter
  @Setter
  @Parameter(
      required = true,
      name = "platform",
      alias = "platform",
      property = "generate-sfx.platform")
  protected String platform;

  /**
   * Every executable generated must perform some steps upon execution. For instance, it's typical
   * to launch an installer such as IZPack. This file should contain instructions for doing so
   */
  @Getter
  @Setter
  @Parameter(alias = "executable-file", property = "generate-sfx.executable-file")
  protected String executableFile;

  /**
   * the archive-directory property specifies which directory we're archiving and making executable
   */
  @Getter
  @Setter
  @Parameter(
      name = "archive-directory",
      alias = "archive-directory",
      property = "generate-sfx.archive-directory",
      defaultValue = "${project.basedir}/archive")
  protected File archiveDirectory;

  /** this property specifies which directory we're placing the resulting executable archive into */
  @Getter
  @Setter
  @Parameter(
      required = true,
      name = "workspace",
      alias = "workspace",
      property = "generate-sfx.workspace",
      defaultValue = "${project.build.directory}/sfx-workspace")
  protected File workspace;

  @Getter
  @Setter
  @Parameter(
      name = "executable-configuration",
      alias = "executable-configuration",
      property = "generate-sfx.executable-configuration")
  protected ExecutableConfiguration executableConfiguration;
}
