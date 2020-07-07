package io.zephyr.bundle.sfx;

import io.zephyr.bundle.sfx.icons.CompositeIconDefinition;
import java.io.File;
import java.util.Map;

public interface ExecutableFileConfiguration {

  /** @return the version string to set on this executable file/package */
  String getVersionString();

  /** @return the file version to set on this executable file/package */
  String getFileVersion();

  /** @return the product version to set on this executable file/package */
  String getProductVersion();

  /** @return the platform-specific manifest file for this archive */
  File getManifestFile();

  /**
   * @return the resource strings for this executable package. These may be resource strings for
   *     Windows portable executable files (PE files), or they may be Mac OSX entitlement files
   */
  Map<String, String> getResourceStrings();

  /** @return the icon definition for the current platform/executable */
  CompositeIconDefinition getIconDefinition();
}
