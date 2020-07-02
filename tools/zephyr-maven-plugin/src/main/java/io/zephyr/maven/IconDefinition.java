package io.zephyr.maven;

import io.zephyr.bundle.sfx.ConfigurationObject;
import io.zephyr.bundle.sfx.icons.BundleIconDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

public class IconDefinition implements ConfigurationObject<BundleIconDefinition> {

  enum Format {
    ICO,
    ICNS;

    public static BundleIconDefinition.Format mapFormat(Format format) {
      switch (format) {
        case ICO:
          return BundleIconDefinition.Format.ICO;
        case ICNS:
          return BundleIconDefinition.Format.ICNS;
      }
      throw new IllegalArgumentException("No valid icon '" + format + "'");
    }
  }

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
  public BundleIconDefinition toCoreObject() {
    val result = new BundleIconDefinition();
    result.setIcons(Icon.mapIcons(icons));
    result.setFormat(Format.mapFormat(format));
    result.setSource(source);
    return result;
  }
}
