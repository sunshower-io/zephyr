package io.zephyr.kernel.core;

import io.zephyr.common.io.Files;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.classloading.KernelClassloader;
import io.zephyr.kernel.concurrency.*;
import io.zephyr.kernel.concurrency.Process;
import io.zephyr.kernel.core.actions.ModuleInstallationCompletionPhase;
import io.zephyr.kernel.core.actions.WritePluginDescriptorPhase;
import io.zephyr.kernel.core.lifecycle.DefaultKernelLifecycle;
import io.zephyr.kernel.events.AbstractEventSource;
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
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

@SuppressWarnings({
  "PMD.AvoidUsingVolatile",
  "PMD.DoNotUseThreads",
  "PMD.UnusedPrivateMethod",
  "PMD.AvoidInstantiatingObjectsInLoops"
})
public class SunshowerKernel extends AbstractEventSource implements Kernel {

  static final Logger log = Logging.get(SunshowerKernel.class);

  /** class fields */
  @Setter private static KernelOptions kernelOptions;

  private final Scheduler<String> scheduler;
  private final ModuleClasspathManager moduleClasspathManager;
  @Getter private final ModuleManager moduleManager;
  private final KernelLifecycle lifecycle;
  /** Instance fields */
  private volatile ClassLoader classLoader;

  @Getter @Setter private volatile FileSystem fileSystem;

  @Inject
  public SunshowerKernel(
      ModuleClasspathManager moduleClasspathManager,
      ModuleManager moduleManager,
      Scheduler<String> scheduler) {
    this.scheduler = scheduler;
    this.moduleManager = moduleManager;
    this.moduleClasspathManager = moduleClasspathManager;
    this.lifecycle = new DefaultKernelLifecycle(this, scheduler);
  }

  public static KernelOptions getKernelOptions() {
    if (kernelOptions == null) {
      throw new IllegalStateException("Error: KernelOptions are null--this is definitely a bug");
    }
    return kernelOptions;
  }

  public static void setKernelOptions(KernelOptions options) {
    kernelOptions = options;
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
  public <T> List<T> locateServices(Class<T> type) {
    val result = new ArrayList<T>();
    load(result, type, getClassLoader());

    return result;
  }

  @Override
  @SneakyThrows
  public void start() {
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
    lifecycle.stop().toCompletableFuture().get();
  }

  @Override
  public ModuleClasspathManager getModuleClasspathManager() {
    return moduleClasspathManager;
  }

  @Override
  public Scheduler<String> getScheduler() {
    return scheduler;
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
      val result = Plugins.getFileSystem(coordinate);
      log.log(Level.INFO, "plugin.fs.hydration.succeeded", new Object[] {coordinate, result.fst});
      return result.snd;
    } catch (IOException ex) {
      log.log(
          Level.WARNING, "plugin.fs.hydration.failed", new Object[] {coordinate, ex.getMessage()});
      throw ex;
    }
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
}
