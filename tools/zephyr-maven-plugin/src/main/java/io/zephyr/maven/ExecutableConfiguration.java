package io.zephyr.maven;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

@ToString
public class ExecutableConfiguration {

  /** the version string to set in an executable */
  @Getter
  @Setter
  @Parameter(
      name = "version-string",
      alias = "version-string",
      property = "generate-sfx.version-string")
  private String versionString;

  @Getter
  @Setter
  @Parameter(name = "file-version", alias = "file-version", property = "generate-sfx.file-version")
  private String fileVersion;

  @Getter
  @Setter
  @Parameter(
      name = "product-version",
      alias = "product-version",
      property = "generate-sfx.product-version")
  private String productVersion;

  @Getter
  @Setter
  @Parameter(
      name = "manifest-file",
      alias = "manifest-file",
      property = "generate-sfx.manifest-file")
  private File manifestFile;

  @Getter
  @Setter
  @Parameter(
      name = "resource-strings",
      alias = "resource-strings",
      property = "generate-sfx.resource-strings")
  private Map<String, String> resourceStrings;

  @Getter
  @Setter
  @Parameter(
      name = "icon-definition",
      alias = "icon-definition",
      property = "generate-sfx.icon-definition")
  private IconDefinition iconDefinition;
}
