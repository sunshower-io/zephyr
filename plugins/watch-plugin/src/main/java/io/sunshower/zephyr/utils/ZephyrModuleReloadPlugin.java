package io.sunshower.zephyr.utils;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.Source;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import io.zephyr.kernel.module.ModuleLifecycle.Actions;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import lombok.extern.java.Log;
import lombok.val;

@Log
public class ZephyrModuleReloadPlugin implements ModuleActivator {

  private final List<WatchSet> existing;
  private Kernel kernel;
  private volatile boolean running;

  public ZephyrModuleReloadPlugin() {
    existing = new ArrayList<>();
  }

  @Override
  public void start(ModuleContext moduleContext) throws Exception {
    this.kernel = moduleContext.unwrap(Kernel.class);
    log.info("Starting Module Reload Module");
    registerWatch(kernel);
  }

  @Override
  public void stop(ModuleContext moduleContext) throws Exception {
    log.info("Stopping Module Reload Module");
    running = false;
  }

  private void registerWatch(Kernel kernel) throws IOException {
    val watchService = FileSystems.getDefault().newWatchService();

    val modules = kernel.getModuleManager().getModules();

    for (val module : modules) {
      registerWatch(module, watchService);
    }
    watch(watchService);
  }

  private void watch(WatchService watchService) throws IOException {
    val keys = collectKeys(watchService);
    running = true;
    new Thread(
            () -> {
              try {
                while (running) {
                  val key = watchService.take();
                  if (key == null) {
                    continue;
                  }
                  val set = keys.get(key);
                  val kinds = new HashSet<Kind<?>>();
                  for (val event : key.pollEvents()) {
                    log.log(
                        Level.INFO,
                        "Received modification event {0} on directory {1} (module {2})",
                        new Object[] {event.kind(), set.directory, set.module});
                    kinds.add(event.kind());
                  }
                  performModuleLifecycle(set, kinds);
                  key.reset();
                }
              } catch (Exception ex) {

              }
            })
        .start();
  }

  private void performModuleLifecycle(WatchSet set, HashSet<Kind<?>> kinds) throws Exception {
    if (kinds.contains(ENTRY_DELETE)) {
      removeModule(set);
    }
    if (kinds.contains(ENTRY_CREATE)) {
      installModule(set);
    }
    if (kinds.contains(ENTRY_MODIFY)) {
      reinstallModule(set);
    }
  }

  private void reinstallModule(WatchSet set) throws Exception {
    removeModule(set);
    installModule(set);
  }

  private void installModule(WatchSet set) throws Exception {
    val grp = new ModuleInstallationGroup();
    val req = new ModuleInstallationRequest();
    req.setLocation(set.moduleSource.toURI().toURL());
    grp.add(req);
    kernel.getModuleManager().prepare(grp).commit().toCompletableFuture().get();
    startModule(set);
  }

  private void startModule(WatchSet set) throws ExecutionException, InterruptedException {
    val grp = new ModuleLifecycleChangeGroup();
    val req = new ModuleLifecycleChangeRequest(set.module.getCoordinate(), Actions.Activate);
    grp.addRequest(req);
    kernel.getModuleManager().prepare(grp).commit().toCompletableFuture().get();
  }

  private void removeModule(WatchSet set) throws ExecutionException, InterruptedException {
    val grp = new ModuleLifecycleChangeGroup();
    val req = new ModuleLifecycleChangeRequest(set.module.getCoordinate(), Actions.Delete);
    grp.addRequest(req);
    kernel.getModuleManager().prepare(grp).commit().toCompletableFuture().get();
  }

  private Map<WatchKey, WatchSet> collectKeys(WatchService watchService) {
    val result = new HashMap<WatchKey, WatchSet>();
    for (val set : existing) {
      try {
        result.put(
            set.directory.toPath().register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE),
            set);
      } catch (Exception ex) {
        log.log(
            Level.WARNING, "Failed to register watch service for directory: {0}.  Continuing", ex);
      }
    }
    return result;
  }

  private void registerWatch(Module module, WatchService watchService) {
    val source = module.getSource(); // embedded plugins may have a null source
    if (source != null) {
      existing.add(getLocation(module, source));
    }
  }

  private WatchSet getLocation(Module module, Source source) {
    log.log(Level.INFO, "Resolving installation source: {0}", source.getLocation());
    val file = new File(source.getLocation().getPath());
    val parent = file.getParentFile();
    log.log(
        Level.INFO,
        "Resolved parent source {0} from assembly root {1}",
        new Object[] {source.getLocation(), parent});
    return new WatchSet(parent, file, module);
  }

  static final class WatchSet {

    final File directory;
    final Module module;
    final File moduleSource;

    WatchSet(File directory, File moduleSource, Module module) {
      this.module = module;
      this.directory = directory;
      this.moduleSource = moduleSource;
    }
  }
}
