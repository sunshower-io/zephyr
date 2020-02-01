package io.zephyr.scan;

import static java.nio.file.StandardWatchEventKinds.*;

import io.zephyr.cli.DefaultZephyr;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPointRegistry;
import io.zephyr.kernel.log.Logging;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class DirectoryScanner implements EntryPoint {
  static final Logger log = Logging.get(DirectoryScanner.class);

  final Map<WatchKey, Path> keys;

  private FileSystem fileSystem;
  private DirectoryScannerOptions options;
  private WatchService watchService;
  volatile boolean running;

  public DirectoryScanner() {
    keys = new HashMap<>(0);
  }

  @Override
  public void run(Map<ContextEntries, Object> ctx) {
    EntryPointRegistry registry = (EntryPointRegistry) ctx.get(ContextEntries.ENTRY_POINT_REGISTRY);
    if (registry == null) {
      log.log(Level.WARNING, "scanner.entrypoint.noregistry");
      return;
    }

    val epoints = registry.getEntryPoints(this::exportsKernel);

    if (epoints.isEmpty()) {
      log.log(Level.WARNING, "scanner.entrypoint.nokernel");
    }

    val kernel = epoints.iterator().next().getService(Kernel.class);
    val zephyr = new DefaultZephyr(kernel);

    running = true;
    try {
      doRun(kernel, zephyr);

    } catch (IOException ex) {
      log.log(Level.WARNING, "scanner.entrypoint.scanfailed", ex);
    }
  }

  private void doRun(Kernel kernel, Zephyr zephyr) throws IOException {
    fileSystem = kernel.getFileSystem();
    watchService = fileSystem.newWatchService();
    registerKeys(watchService);

    watch(zephyr, watchService);
  }

  private void watch(Zephyr zephyr, WatchService watchService) {
    while (running) {
      try {
        val key = watchService.take();
        doHandle(zephyr, key, watchService);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
        return;
      }
    }
  }

  private void doHandle(Zephyr zephyr, WatchKey key, WatchService watchService) {
    for (val event : key.pollEvents()) {
      handleEvent(zephyr, key, event, watchService);
    }
  }

  private void registerKeys(WatchService watchService) throws IOException {
    val paths = options.getDirectories();
    if (paths == null || paths.length == 0) {
      register(fileSystem.getPath("deployments"), watchService);
      return;
    } else {
      for (val path : paths) {
        register(Path.of(path), watchService);
      }
    }
  }

  private void register(Path path, WatchService watchService) throws IOException {
    log.log(Level.INFO, "scanner.watching.path", path);
    val key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    keys.put(key, path);
  }

  public void stop() {
    try {
      watchService.close();
    } catch (IOException e) {
      log.log(Level.WARNING, "scanner.watchservice.close.failed", e);
    } finally {
      running = false;
    }
  }

  @Override
  public void initialize(Map<ContextEntries, Object> context) {
    val args = context.get(ContextEntries.ARGS);
    if (args == null || ((String[]) args).length == 0) {
      options = new DirectoryScannerOptions();
    } else {
      options = io.zephyr.common.Options.create(DirectoryScannerOptions::new, context);
    }
  }

  private boolean exportsKernel(EntryPoint entryPoint) {
    return entryPoint.getService(Kernel.class) != null;
  }

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  public DirectoryScannerOptions getOptions() {
    return options;
  }

  @Override
  public int getPriority() {
    return 100;
  }

  private void handleEvent(
      Zephyr zephyr, WatchKey key, WatchEvent<?> event, WatchService watchService) {
    if (event.kind() == ENTRY_CREATE) {
      handleCreate(zephyr, key, event, watchService);
    }
    if (event.kind() == ENTRY_DELETE) {
      handleDelete(zephyr, key, event, watchService);
    }
    if (event.kind() == ENTRY_MODIFY) {
      handleDelete(zephyr, key, event, watchService);
    }
  }

  private void handleModify(WatchEvent<?> event, WatchService watchService) {}

  private void handleDelete(
      Zephyr zephyr, WatchKey key, WatchEvent<?> event, WatchService watchService) {

    val path = keys.get(key);
    val deployedFile = path.resolve((Path) event.context());
    log.log(Level.INFO, "scanner.deployment.removal.detected", deployedFile);
    try {

      val file = deployedFile.toAbsolutePath().toFile();
      val modules = zephyr.getPlugins();
      for (val module : modules) {
        if (module.getSource().is(file)) {
          zephyr.remove(module.getCoordinate().toCanonicalForm());
          break;
        }
      }
    } catch (Exception ex) {
      log.log(Level.WARNING, "scanner.deployment.removal.failed", path);
    }
  }

  private void handleCreate(
      Zephyr zephyr, WatchKey key, WatchEvent<?> event, WatchService watchService) {
    val path = keys.get(key);
    val deployedFile = path.resolve((Path) event.context());
    log.log(Level.INFO, "scanner.deployment.detected", deployedFile);
    try {
      zephyr.install(deployedFile.toAbsolutePath().toFile().toURI().toURL());
    } catch (Exception ex) {
      log.log(Level.WARNING, "scanner.deployment.failed", path);
    }
  }
}
