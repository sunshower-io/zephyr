package io.sunshower.kernel;

public interface PluginListener {
  /**
   * Fired when a plugin is loaded
   *
   * @param descriptor
   */
  default void onPluginLoaded(PluginDescriptor descriptor) {}

  /**
   * Fired with a plugin is unloaded
   *
   * @param descriptor
   */
  default void onPluginUnloaded(PluginDescriptor descriptor) {}

  /**
   * @param descriptor the descriptor
   * @param ex the error
   */
  default void onPluginError(PluginDescriptor descriptor, Throwable ex) {}
}
