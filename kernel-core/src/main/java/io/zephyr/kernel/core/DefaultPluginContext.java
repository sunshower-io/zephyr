package io.zephyr.kernel.core;

import io.zephyr.api.*;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.ModuleException;
import io.zephyr.kernel.concurrency.AsynchronousModuleThreadTracker;
import io.zephyr.kernel.concurrency.AsynchronousServiceTracker;
import io.zephyr.kernel.concurrency.ModuleThread;
import io.zephyr.kernel.extensions.ExpressionLanguageExtension;
import io.zephyr.kernel.log.Logging;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class DefaultPluginContext implements ModuleContext {

  final Module module;
  final Kernel kernel;

  static final Logger log = Logging.get(DefaultPluginContext.class);

  public DefaultPluginContext(final Module module, final Kernel kernel) {
    this.module = module;
    this.kernel = kernel;
  }

  @Override
  public <T> RequirementRegistration<T> createRequirement(Requirement<T> requirement) {
    return null;
  }

  /**
   * this resolves an expression language as follows:
   *
   * <p>1. search through the installed modules 2. search through the kernel
   *
   * @param query
   * @param <T>
   * @return
   */
  @Override
  public <T> Predicate<T> createFilter(Query<T> query) {
    var ext = resolveModuleExpressionLanguageExtension(query);
    if (ext == null) {
      ext = resolveKernelModuleExpressionLanguageExtension(query);
    }
    if (ext == null) {
      throw new ModuleException(
          "Unable to locate extension for expression language: " + query.getLanguage());
    }
    return ext.createPredicate(query);
  }

  @Override
  public <T> CapabilityRegistration<T> provide(CapabilityDefinition<T> capability) {
    return null;
  }

  @Override
  public Module getModule() {
    return module;
  }

  @Override
  public List<Module> getModules(Predicate<Module> filter) {
    return kernel.getModuleManager().getModules();
  }

  @Override
  public ModuleTracker trackModules(Predicate<Module> filter) {
    return new AsynchronousModuleThreadTracker(
        kernel, module, (ModuleThread) module.getTaskQueue(), filter);
  }

  @Override
  public ModuleTracker trackModules(Query<Module> filter) {
    return trackModules(createFilter(filter));
  }

  @Override
  public ServiceTracker trackServices(Query<ServiceReference<?>> filter) {
    val predicate = createFilter(filter);
    return trackServices(predicate);
  }

  @Override
  public ServiceTracker trackServices(Predicate<ServiceReference<?>> filter) {
    return new AsynchronousServiceTracker(kernel, filter);
  }

  @Override
  public <T> ServiceRegistration<T> register(ServiceDefinition<T> definition) {
    return null;
  }

  @Override
  public <T> List<ServiceReference<T>> getReferences(Class<T> type) {
    return null;
  }

  @Override
  public <T> List<ServiceReference<T>> getReferences(Query<ServiceDefinition<T>> query) {
    return null;
  }

  private <T> ExpressionLanguageExtension resolveModuleExpressionLanguageExtension(Query<T> query) {
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "el.locating.evaluator.modules");
    }
    for (val module : kernel.getModuleManager().getModules()) {
      val lifecycle = module.getLifecycle();
      val state = lifecycle.getState();
      if (state.isAtLeast(Lifecycle.State.Active)) {
        val serviceLoader = resolveModuleExpressionLanguageExtensionInModule(query, module);
        if (serviceLoader != null) return serviceLoader;
      }
    }
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "el.locating.evaluator.modules.failed");
    }
    return null;
  }

  private <T> ExpressionLanguageExtension resolveModuleExpressionLanguageExtensionInModule(
      Query<T> query, Module module) {
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "el.locating.evaluator.module", module.getCoordinate());
    }
    val serviceLoaders = module.resolveServiceLoader(ExpressionLanguageExtension.class);
    for (val serviceLoader : serviceLoaders) {
      if (serviceLoader.supports(query)) {
        if (log.isLoggable(Level.FINE)) {
          log.log(Level.FINE, "el.located.evaluator.module", module.getCoordinate());
        }
        return serviceLoader;
      }
    }
    return null;
  }

  private <T> ExpressionLanguageExtension resolveKernelModuleExpressionLanguageExtension(
      Query<T> query) {
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "el.locating.evaluator.kernelmodule");
    }
    val kernelServiceLoaders =
        ServiceLoader.load(ExpressionLanguageExtension.class, kernel.getClassLoader());

    for (val serviceLoader : kernelServiceLoaders) {
      if (serviceLoader.supports(query)) {
        if (log.isLoggable(Level.FINE)) {
          log.log(Level.FINE, "el.located.evaluator.kernelmodule");
        }
        return serviceLoader;
      }
    }
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "el.locating.evaluator.kernelmodule.failed");
    }
    return null;
  }
}
