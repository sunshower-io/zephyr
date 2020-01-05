package io.zephyr;

/**
 * All exceptions must be handled by the kernel, which is why we do not restrict the exception type
 * to KernelException.
 */
public interface PluginActivator {

  default void initialize(PluginContext ctx) throws Exception {}

  void start(PluginContext context) throws Exception;

  void stop(PluginContext context) throws Exception;
}
