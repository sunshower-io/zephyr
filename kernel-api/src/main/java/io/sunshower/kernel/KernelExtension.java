package io.sunshower.kernel;

import java.nio.file.Path;

public interface KernelExtension {

  enum State {
    /** Loaded but not resolved */
    Loaded,
    /** Loaded + Resolved */
    Resolved,
    /** Loaded + Resolved but passivated */
    Passive,

    /** Loaded + Resolved but activated */
    Active,
  }

  /** @return the registration for this extension */
  KernelExtensionRegistration getRegistration();

  /** @return the current lifecycle state of this extension */
  State getState();

  /** @return the module defined by this extension */
  Module getModule();

  /** @return the location (if any) this extension places its data into */
  Path getWorkspaceDirectory();

  /** @return the location that this extension is installed to (post-load) */
  Path getInstallationDirectory();
}
