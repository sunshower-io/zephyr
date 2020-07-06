package io.zephyr.bundle.sfx.icons;

import io.zephyr.bundle.sfx.SelfExtractingExecutableConfiguration;
import java.io.File;
import java.util.List;

public interface CompositeIconDefinition {

  /**
   * which format are we specificially targeting? Note that these don't really have any implications
   * for Linux platforms at the moment
   */
  enum Format {
    ICO,
    ICNS
  }

  /**
   * @return the source icon for this icon definition. We generate the platform-specific composite
   *     icons automatically
   */
  File getSource();

  /**
   * @return the platform-specific icon format to generate. We may not really need this as this can
   *     strictly be inferred from {@link SelfExtractingExecutableConfiguration#getPlatform()}
   *     <p>for now, we generally throw an exception when format and platform are inconsistent with
   *     each other
   */
  Format getFormat();

  /** @return the collection of icon definitions to include in this composite icon */
  List<? extends IconDefinition> getIconDefinitions();
}
