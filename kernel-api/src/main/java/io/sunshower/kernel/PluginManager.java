package io.sunshower.kernel;

import io.sunshower.kernel.events.PluginEvent;
import java.net.URL;
import java.util.List;

public interface PluginManager
    extends KernelExtensionManager<
        PluginEventListener, PluginEvent, PluginDescriptor, PluginLoadTask> {

  /** @return the plugins that are currently loaded. */
  List<PluginDescriptor> getLoaded();

  /**
   * @param descriptor
   * @return true if the plugin was unloaded
   * @throws PluginException if the plugin is not stopped or cannot be unloaded
   */
  boolean unload(PluginDescriptor descriptor) throws PluginException;

  /**
   * @param url the URL to load the plugin from
   * @return the plugin descriptor
   * @throws PluginConflictException if anything happens while the plugin is being loaded
   */
  PluginLoadTask loadExtensionFile(URL url) throws KernelExtensionException;
}
