package io.zephyr.kernel.core;

import io.zephyr.api.ModuleContext;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.common.io.Files;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.KernelModuleEntry;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.classloading.KernelClassloader;
import io.zephyr.kernel.concurrency.*;
import io.zephyr.kernel.concurrency.Process;
import io.zephyr.kernel.core.actions.ModuleInstallationCompletionPhase;
import io.zephyr.kernel.core.actions.WritePluginDescriptorPhase;
import io.zephyr.kernel.core.lifecycle.DefaultKernelLifecycle;
import io.zephyr.kernel.events.*;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.memento.Memento;
import io.zephyr.kernel.memento.MementoProvider;
import io.zephyr.kernel.memento.Mementos;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import lombok.*;

@SuppressWarnings({
  "PMD.AvoidUsingVolatile",
  "PMD.DoNotUseThreads",
  "PMD.UnusedPrivateMethod",
  "PMD.AvoidInstantiatingObjectsInLoops"
})
public class SunshowerKernel implements Kernel, EventSource {

  static final Logger log = Logging.get(SunshowerKernel.class);

  /** class fields */
  private static KernelOptions kernelOptions;

  /** @return the kernel options used to start this instance. */
  @NonNull
  public static KernelOptions getKernelOptions() {
    if (kernelOptions == null) {
      throw new IllegalStateException("Error: KernelOptions are null--this is definitely a bug");
    }
    return kernelOptions;
  }

  /** Instance fields */
  private volatile ClassLoader classLoader;

  private final KernelLifecycle lifecycle;
  private final Scheduler<String> scheduler;
  private final ServiceRegistry serviceRegistry;
  private final AsynchronousEventSource eventDispatcher;

  /** accessable fields */
  @Getter private final ModuleManager moduleManager;

  @Setter private ModuleClasspathManager moduleClasspathManager;

  /** mutable fields */
  @Getter @Setter private volatile FileSystem fileSystem;

  @Inject
  public SunshowerKernel(
      ModuleManager moduleManager,
      ServiceRegistry registry,
      Scheduler<String> scheduler,
      ClassLoader parentClassloader) {
    this.scheduler = scheduler;
    this.serviceRegistry = registry;
    this.moduleManager = moduleManager;
    this.lifecycle = new DefaultKernelLifecycle(this, scheduler, parentClassloader);
    this.eventDispatcher = new AsynchronousEventSource(scheduler.getKernelExecutor());
  }

  public SunshowerKernel(
      ModuleManager moduleManager, ServiceRegistry registry, Scheduler<String> scheduler) {
    this(moduleManager, registry, scheduler, Thread.currentThread().getContextClassLoader());
  }

  public static void setKernelOptions(KernelOptions options) {
    kernelOptions = options;
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  @Override
  public List<KernelModuleEntry> getKernelModules() {
    return ((KernelClassloader) classLoader).getKernelModules();
  }

  @Override
  public KernelLifecycle getLifecycle() {
    return lifecycle;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public void setClassLoader(KernelClassloader loader) {
    this.classLoader = loader;
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public <T> List<T> locateServices(Class<T> type) {
    synchronized (this) {
      val currentThread = Thread.currentThread();
      val kernelClassloader = getClassLoader();
      val currentContextClassloader = currentThread.getContextClassLoader();
      try {
        val result = new ArrayList<T>();
        currentThread.setContextClassLoader(kernelClassloader);
        load(result, type, getClassLoader());
        return result;
      } finally {
        Thread.currentThread().setContextClassLoader(currentContextClassloader);
      }
    }
  }

  @Override
  @SneakyThrows
  public void start() {
    serviceRegistry.initialize(this);
    eventDispatcher.start();
    lifecycle.start().toCompletableFuture().get();
  }

  @Override
  public void reload() {
    stop();
    start();
  }

  @Override
  @SneakyThrows
  public void stop() {
    eventDispatcher.stop();
    lifecycle.stop().toCompletableFuture().get();
    serviceRegistry.close();
  }

  @Override
  public ModuleClasspathManager getModuleClasspathManager() {
    return moduleClasspathManager;
  }

  @Override
  public Scheduler<String> getScheduler() {
    return scheduler;
  }

  @Override
  public ModuleContext createContext(Module module) {
    val ctx = new DefaultPluginContext(module, this);
    ((AbstractModule) module).setContext(ctx);
    return ctx;
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private <T> void load(List<T> result, Class<T> type, ClassLoader classLoader) {
    val loader = ServiceLoader.load(type, classLoader);
    for (val service : loader) {
      result.add(service);
    }
  }

  @Override
  public Memento save() {
    val memento = Memento.load(getClassLoader());
    memento.write("state", getLifecycle().getState());
    writePlugins(memento.child("plugins"));
    return memento;
  }

  @Override
  public CompletionStage<Void> persistState() throws Exception {
    val memento = save();
    val file = memento.locate("kernel", getFileSystem());
    Files.tryWrite(file, memento);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletionStage<Void> restoreState() throws Exception {
    val mementoProvider = Memento.loadProvider(getClassLoader());
    val kernelMemento = mementoProvider.newMemento("kernel", "kernel", getFileSystem());
    return doRestore(kernelMemento);
  }

  @Override
  public int getListenerCount() {
    return eventDispatcher.getListenerCount();
  }

  @Override
  public boolean listensFor(EventType... types) {
    return eventDispatcher.listensFor(types);
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, EventType... types) {
    eventDispatcher.addEventListener(listener, types);
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, int options, EventType... types) {
    eventDispatcher.addEventListener(listener, options, types);
  }

  @Override
  public <T> void removeEventListener(EventListener<T> listener) {
    eventDispatcher.removeEventListener(listener);
  }

  @Override
  public <T> void dispatchEvent(EventType type, Event<T> event) {
    eventDispatcher.dispatchEvent(type, event);
  }

  /** private methods */
  private void writePlugins(Memento pluginsMemento) {
    val plugins = moduleManager.getModules();
    for (val plugin : plugins) {
      val pluginMemento = pluginsMemento.child("plugin");
      val coordinate = plugin.getCoordinate();
      val state = plugin.getLifecycle().getState();
      Mementos.writeCoordinate(pluginMemento, coordinate);
      pluginMemento.write("state", state);
    }
  }

  @Override
  public void restore(Memento memento) {
    try {
      doRestore(memento).toCompletableFuture().get();
    } catch (Exception ex) {
      log.log(Level.WARNING, "failed to restore kernel state.  Reason: {0}", ex.getMessage());
      if (log.isLoggable(Level.FINE)) {
        log.log(Level.FINE, "Reason: ", ex);
      }
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private CompletionStage<Void> doRestore(Memento memento) {
    val pluginsMemento = memento.childNamed("plugins");
    val pluginMementoProvider = Memento.loadProvider(getClassLoader());
    val pluginMementos = pluginsMemento.getChildren("plugin");

    val modules = new HashSet<Module>(pluginMementos.size());
    val stateMap = new HashMap<Coordinate, Lifecycle.State>(pluginMementos.size());

    for (val pluginMemento : pluginMementos) {
      val plugin = hydrate(pluginMementoProvider, pluginMemento, modules);
      stateMap.put(
          plugin.getCoordinate(),
          Lifecycle.State.valueOf(pluginMemento.read("state", String.class)));
    }

    val process =
        Tasks.newProcess("kernel:module:load")
            .register(new WritePluginDescriptorPhase("kernel:module:descriptors:load"))
            .create();
    process.getContext().set(ModuleInstallationCompletionPhase.INSTALLED_PLUGINS, modules);
    process.getContext().set("SunshowerKernel", this);
    try {
      scheduler.submit(process).toCompletableFuture().get();
      return requestStart(modules, stateMap).thenAccept(t -> {});
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
      // todo: we're in a jacked state rn
    }
  }

  private CompletionStage<Process<String>> requestStart(
      Set<Module> modules, Map<Coordinate, Lifecycle.State> stateMap) {
    val startGroup = new ModuleLifecycleChangeGroup();
    for (val module : modules) {
      val state = stateMap.get(module.getCoordinate());
      if (state == Lifecycle.State.Active) {
        val request =
            new ModuleLifecycleChangeRequest(
                module.getCoordinate(), ModuleLifecycle.Actions.Activate);
        startGroup.addRequest(request);
      }
    }
    return moduleManager.prepare(startGroup).commit();
  }

  private Module hydrate(
      MementoProvider pluginMementoProvider, Memento pluginMemento, Set<Module> modules) {
    try {

      val coordinate = pluginMemento.read("coordinate", Coordinate.class);
      val filesystem = hydrateFilesystem(coordinate);
      val plugin = new DefaultModule();

      val lifecycle = new ModuleLifecycle(plugin);
      lifecycle.setState(Lifecycle.State.Installed);
      plugin.setLifecycle(lifecycle);

      plugin.setCoordinate(coordinate);
      plugin.setKernel(this);
      plugin.setFileSystem(filesystem);

      hydratePlugin(pluginMementoProvider, plugin, filesystem);
      modules.add(plugin);
      return plugin;
    } catch (IOException ex) {
      throw new RuntimeException(ex);
      // todo: handle fs create failed
    } catch (Exception ex) {
      throw new RuntimeException(ex);
      // todo: handle plugin hydration failed
    }
  }

  private void hydratePlugin(
      MementoProvider pluginMementoProvider, DefaultModule plugin, FileSystem fs) throws Exception {
    val pluginMemento =
        pluginMementoProvider.newMemento("plugin", plugin.getCoordinate().toCanonicalForm(), fs);
    plugin.restore(pluginMemento);
  }

  private FileSystem hydrateFilesystem(Coordinate coordinate) throws IOException {
    try {
      val result = Plugins.getFileSystem(coordinate, this);
      log.log(Level.INFO, "plugin.fs.hydration.succeeded", new Object[] {coordinate, result.fst});
      return result.snd;
    } catch (IOException ex) {
      log.log(
          Level.WARNING, "plugin.fs.hydration.failed", new Object[] {coordinate, ex.getMessage()});
      throw ex;
    }
  }
}
