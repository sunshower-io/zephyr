package io.zephyr.kernel.core;

import io.zephyr.api.*;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.ModuleException;
import io.zephyr.kernel.VolatileStorage;
import io.zephyr.kernel.concurrency.AsynchronousModuleThreadTracker;
import io.zephyr.kernel.concurrency.AsynchronousServiceTracker;
import io.zephyr.kernel.concurrency.ModuleThread;
import io.zephyr.kernel.extensions.ExpressionLanguageExtension;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.service.DefaultServiceDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class DefaultPluginContext implements ModuleContext {

  final Module module;
  final Kernel kernel;

  final VolatileStorage delegate;
  final Map<Object, Object> context;

  static final Object lock = new Object();
  static final Logger log = Logging.get(DefaultPluginContext.class);

  public DefaultPluginContext(
      final Module module, final Kernel kernel, final VolatileStorage delegate) {
    this.module = module;
    this.kernel = kernel;
    this.delegate = delegate;
    this.context = new ConcurrentHashMap<>();
  }

  @Override
  public <T> RequirementRegistration<T> createRequirement(Requirement<T> requirement) {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> type) {
    val storage = kernel.getVolatileStorage();
    if (storage.contains(type)) {
      return storage.get(type);
    }
    return (T) kernel;
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
    return new AsynchronousServiceTracker(kernel, module, module.getTaskQueue(), filter);
  }

  @Override
  public <T> ServiceRegistration<T> register(ServiceDefinition<T> definition) {
    return kernel.getServiceRegistry().register(module, definition);
  }

  @Override
  public <T> ServiceRegistration<T> register(Class<T> type, String name, T value) {
    return register(new DefaultServiceDefinition<>(type, name, value));
  }

  @Override
  public <T> ServiceRegistration<T> register(Class<T> type, T value) {
    return register(new DefaultServiceDefinition<>(type, value));
  }

  @Override
  public <T> ServiceRegistration<T> register(Class<T> type, String name, Supplier<T> factory) {
    return register(new FactoryServiceDefinition<T>(type, name, factory));
  }

  @Override
  public <T> ServiceRegistration<T> register(Class<T> type, Supplier<T> factory) {
    return register(new FactoryServiceDefinition<T>(type, factory));
  }

  @Override
  @SuppressWarnings({"unchecked", "PMD.DataflowAnomalyAnalysis"})
  public <T> List<ServiceReference<T>> getReferences(Class<T> type) {
    synchronized (lock) {
      val moduleManager = kernel.getModuleManager();
      val serviceRegistry = kernel.getServiceRegistry();
      val result = new ArrayList<ServiceReference<T>>();
      for (val module : moduleManager.getModules(Lifecycle.State.Active)) {
        val set = serviceRegistry.getRegistrations(module);
        if (set != null) {
          for (val registration : set) {
            if (registration.provides(type)) {
              result.add((ServiceReference<T>) registration.getReference());
            }
          }
        }
      }
      return result;
    }
  }

  @Override
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
  public List<ServiceReference<?>> getReferences(Query<ServiceDefinition<?>> query) {
    synchronized (lock) {
      val predicate = createFilter(query);
      val moduleManager = kernel.getModuleManager();
      val serviceRegistry = kernel.getServiceRegistry();
      val result = new ArrayList<ServiceReference<?>>();
      for (val module : moduleManager.getModules(Lifecycle.State.Active)) {
        for (val registration : serviceRegistry.getRegistrations(module)) {
          val ref = registration.getReference();
          if (predicate.test(ref.getDefinition())) {
            result.add(registration.getReference());
          }
        }
      }
      return result;
    }
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

  @Override
  public <K, V> V get(K key) {
    if (delegate.contains(key)) {
      return delegate.get(key);
    }

    if (kernel.getVolatileStorage().contains(key)) {
      return kernel.getVolatileStorage().get(key);
    }
    return null;
  }

  @Override
  public <K, V> V set(K key, V value) {
    if (delegate.contains(key)) {
      return delegate.set(key, value);
    }
    if (kernel.getVolatileStorage().contains(key)) {
      return kernel.getVolatileStorage().get(key);
    }
    return delegate.set(key, value);
  }

  @Override
  public <K> boolean contains(K key) {
    return delegate.contains(key) || kernel.getVolatileStorage().contains(key);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public void start() {
    throw new IllegalStateException("You cannot call start()");
  }

  @Override
  public void stop() {
    throw new IllegalStateException("You cannot call start()");
  }
}
