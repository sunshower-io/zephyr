package io.zephyr.scan;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPointRegistry;
import io.zephyr.kernel.log.Logging;
import lombok.val;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryScanner implements EntryPoint {
  static final Logger log = Logging.get(DirectoryScanner.class);

  private FileSystem fileSystem;
  final Map<WatchKey, Path> keys;
  private DirectoryScannerOptions options;
  private WatchService watchService;

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

    try {
      doRun(kernel);
    } catch (IOException ex) {

    }
  }

  private void doRun(Kernel kernel) throws IOException {
    fileSystem = kernel.getFileSystem();
    watchService = fileSystem.newWatchService();
    registerKeys(watchService);

    watch(watchService);
  }

  private void watch(WatchService watchService) {
    for (; ; ) {
      try {
        val key = watchService.take();
        doHandle(key, watchService);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }

  private void doHandle(WatchKey key, WatchService watchService) {
    for (val event : key.pollEvents()) {
      handleEvent(event, watchService);
    }
  }

  private void handleEvent(WatchEvent<?> event, WatchService watchService) {
    if (event.kind() == ENTRY_CREATE) {
      handleCreate(event, watchService);
    }
    if (event.kind() == ENTRY_DELETE) {
      handleDelete(event, watchService);
    }
    if (event.kind() == ENTRY_MODIFY) {
      handleModify(event, watchService);
    }
  }

  private void handleModify(WatchEvent<?> event, WatchService watchService) {
    
  }

  private void handleDelete(WatchEvent<?> event, WatchService watchService) {

  }

  private void handleCreate(WatchEvent<?> event, WatchService watchService) {

  }

  private void registerKeys(WatchService watchService) throws IOException {
    val paths = options.getDirectories();
    if (paths == null || paths.length == 0) {
      register(fileSystem.getPath("deployment"), watchService);
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
    }
  }

  @Override
  public void initialize(Map<ContextEntries, Object> context) {
    options = io.zephyr.common.Options.create(DirectoryScannerOptions::new, context);
  }

  private boolean exportsKernel(EntryPoint entryPoint) {
    return entryPoint.getService(Kernel.class) != null;
  }

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  public Options<?> getOptions() {
    return options;
  }

  @Override
  public int getPriority() {
    return 100;
  }
}
