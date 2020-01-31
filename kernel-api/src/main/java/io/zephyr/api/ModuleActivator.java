package io.zephyr.api;

/**
 * All exceptions must be handled by the kernel, which is why we do not restrict the exception type
 * to KernelException.
 */
public interface ModuleActivator {

  default void initialize(ModuleContext ctx) throws Exception {}

  void start(ModuleContext context) throws Exception;

  void stop(ModuleContext context) throws Exception;
}
