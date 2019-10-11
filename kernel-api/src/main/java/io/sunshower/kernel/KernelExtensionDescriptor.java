package io.sunshower.kernel;

import java.net.URL;
import java.nio.file.Path;

public interface KernelExtensionDescriptor {

  /**
   * Where'd this come from?
   *
   * @return the source for this extension
   */
  URL getSource();

  /**
   * @return the file this extension was loaded from. Must be a child of <code>getLoadDirectory()
   *     </code>
   * @see io.sunshower.kernel.KernelExtensionDescriptor#getLoadDirectory()
   */
  Path getLoadedFile();

  /** @return the location the extension is loaded to */
  Path getLoadDirectory();

  /** @return the data directory for this extension */
  Path getDataDirectory();
}
