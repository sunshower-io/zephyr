package io.zephyr.scan;

import io.sunshower.gyre.Pair;
import io.zephyr.api.Startable;
import io.zephyr.api.Stoppable;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.core.*;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.module.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public abstract class AbstractDeploymentScanner implements Startable, Stoppable, Runnable {
  static final Logger logger = Logging.get(AbstractDeploymentScanner.class);

  /** final state */
  private final Kernel kernel;

  private final Set<String> paths;
  private final Map<WatchKey, Path> keys;

  /** mutable state */
  private FileSystem fileSystem;

  private WatchService watchService;

  /** concurrent state */
  private volatile boolean running;

  protected AbstractDeploymentScanner(final Kernel kernel, final Collection<String> paths) {
    check(kernel, paths);

    this.kernel = kernel;
    this.keys = new HashMap<>();
    this.paths = Set.copyOf(paths);
  }

  public Set<String> getPaths() {
    return paths;
  }

  protected Path resolve(String file) {
    return fileSystem.getPath(file);
  }

  @Override
  public void start() {
    logger.log(Level.INFO, "deployment.scanner.starting");
    try {
      this.fileSystem = kernel.getFileSystem();
      this.watchService = fileSystem.newWatchService();
      kernel.getScheduler().getKernelExecutor().submit(this);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public Kernel getKernel() {
    return kernel;
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public void run() {
    this.running = true;
    registerKeys();

    poll();
  }

  private void poll() {
    while (running) {
      try {

        val key = watchService.take();

        processKey(key);
      } catch (InterruptedException ex) {
        logger.log(Level.INFO, "deployment.scanning.path.interrupted", ex.getMessage());
      }
    }
  }

  private void processKey(WatchKey key) {
    for (val events : key.pollEvents()) {
      processEvent(events, key);
    }
  }

  private void processEvent(WatchEvent<?> event, WatchKey key) {
    val kind = event.kind();
    val path = keys.get(key);

    if (logger.isLoggable(Level.INFO)) {
      logger.log(Level.INFO, "deployment.events.processing.kind", new Object[] {kind, path});
    }
    val file = (Path) event.context();

    val absolute = path.resolve(file);

    handleEvent(kind, absolute);
  }

  private void handleEvent(WatchEvent.Kind<?> kind, Path absolute) {

    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
      deploy(absolute);
    }
    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
      deploy(absolute);
    }
    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
      undeploy(absolute);
    }
  }

  private void deploy(Path absolute) {
    val undeploymentResult = undeploy(absolute);
    var descriptor = undeploymentResult.snd;
    if (descriptor == null) {
      val desc = loadDescriptor(undeploymentResult.fst);
      if (desc.isEmpty()) {
        logger.log(Level.INFO, "deployment.scan.no.module");
        return;
      } else {
        descriptor = desc.get();
      }
    }

    val moduleInstallation = new ModuleInstallationRequest();
    moduleInstallation.setLocation(urlFor(absolute.toFile()));
    moduleInstallation.setLifecycleActions(ModuleLifecycle.Actions.Activate);
    val group = new ModuleInstallationGroup(moduleInstallation);
    Coordinate finalDescriptor = descriptor;
    kernel
        .getModuleManager()
        .prepare(group)
        .commit()
        .thenApply(t -> this.startModule(finalDescriptor));
  }

  protected Coordinate startModule(Coordinate coordinate) {
    performLifecycleAction(coordinate, ModuleLifecycle.Actions.Activate);
    return coordinate;
  }

  private Pair<File, Coordinate> undeploy(Path absolute) {
    val file = absolute.toFile();
    Optional<Coordinate> descriptor = loadDescriptor(file);

    if (descriptor.isEmpty()) {
      logger.log(Level.INFO, "deployment.scan.no.module");
    } else {

      val moddesc = descriptor.get();
      performLifecycleAction(moddesc, ModuleLifecycle.Actions.Stop);
      return Pair.of(file, moddesc);
    }
    return Pair.of(file, null);
  }

  private void performLifecycleAction(Coordinate coordinate, ModuleLifecycle.Actions actions) {
    val lifecycle = new ModuleLifecycleChangeRequest(coordinate, actions);
    val group = new ModuleLifecycleChangeGroup();
    group.addRequest(lifecycle);
    val result = kernel.getModuleManager().prepare(group).commit();
    if (actions == ModuleLifecycle.Actions.Stop) {
      result.thenRun(() -> performLifecycleAction(coordinate, ModuleLifecycle.Actions.Delete));
    }
  }

  protected Optional<Coordinate> loadDescriptor(File file) {
    Optional<Coordinate> result = loadFile(file);

    if (result.isEmpty()) {
      val modules = kernel.getModuleManager().getModules();
      for (val mod : modules) {
        if (mod.getSource().is(file)) {
          return Optional.of(mod.getCoordinate());
        }
      }
    } else {
      return result;
    }
    return Optional.empty();
  }

  private Optional<Coordinate> loadFile(File file) {
    val loader = ServiceLoader.load(ModuleScanner.class, kernel.getClassLoader());
    return loader.stream()
        .flatMap(
            t -> t.get().scan(file, urlFor(file)).map(ModuleDescriptor::getCoordinate).stream())
        .findAny();
  }

  protected URL urlFor(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      logger.log(Level.WARNING, "failed to obtain url.  Not normal", e);
      return null;
    }
  }

  private void registerKeys() {
    for (val dir : paths) {
      val path = fileSystem.getPath(dir).toAbsolutePath();
      logger.log(Level.INFO, "deployment.scanning.path.attempt", path);
      if (Files.exists(path)) {
        if (Files.isDirectory(path)) {
          logger.log(Level.INFO, "deployment.scanning.path.exists", path);
          try {
            val key =
                path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            keys.put(key, path);
          } catch (IOException ex) {
            logger.log(
                Level.WARNING,
                "deployment.scanning.path.exception",
                new Object[] {path, ex.getLocalizedMessage()});
          }
        } else {
          logger.log(Level.WARNING, "deployment.scanning.path.file");
        }
      }
    }
  }

  private void check(Kernel kernel, Collection<String> paths) {
    Objects.requireNonNull(paths, "paths must not be null");
    Objects.requireNonNull(kernel, "kernel must not be null");
    if (paths.isEmpty()) {
      throw new IllegalArgumentException("Error: paths must not be null or empty");
    }
  }
}
