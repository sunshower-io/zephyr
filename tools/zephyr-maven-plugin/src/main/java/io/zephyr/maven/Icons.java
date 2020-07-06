package io.zephyr.maven;

import io.zephyr.bundle.sfx.icons.CompositeIconDefinition;
import io.zephyr.bundle.sfx.icons.IconDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

public class Icons implements CompositeIconDefinition {

  @Getter
  @Setter
  @Parameter(
      name = "temp-directory",
      alias = "temp-directory",
      property = "generate-sfx.temp-directory",
      defaultValue = "${project.build.directory}.sfx")
  private File tempDirectory;

  @Getter
  @Setter
  @Parameter(name = "format", alias = "format", property = "generate-sfx.format")
  private Format format;

  @Getter
  @Setter
  @Parameter(name = "source", alias = "source", property = "generate-sfx.source")
  private File source;

  @Getter
  @Setter
  @Parameter(name = "icons", alias = "icons", property = "generate-sfx.icons")
  private List<Icon> icons;

  @Override
  public List<? extends IconDefinition> getIconDefinitions() {
    return icons;
  }
}
