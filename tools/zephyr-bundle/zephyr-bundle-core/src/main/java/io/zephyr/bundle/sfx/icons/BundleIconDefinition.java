package io.zephyr.bundle.sfx.icons;

import java.io.File;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class BundleIconDefinition {

  public enum Format {
    ICO,
    ICNS
  }

  @Getter @Setter private File source;
  @Getter @Setter private Format format;

  @Getter @Setter private List<IconComponent> icons;
}
