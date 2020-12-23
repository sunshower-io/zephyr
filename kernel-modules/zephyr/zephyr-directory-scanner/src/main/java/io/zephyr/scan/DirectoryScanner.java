package io.zephyr.scan;

import static java.nio.file.StandardWatchEventKinds.*;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.cli.DefaultZephyr;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.core.Framework;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPointRegistry;
import io.zephyr.kernel.log.Logging;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.val;

public class DirectoryScanner implements EntryPoint, ModuleActivator {
  static final Logger log = Logging.get(DirectoryScanner.class);

  /** immutable state */
  /** currently watched keys */
  final Map<WatchKey, Path> keys;

  /** mutable state */
  private FileSystem fileSystem;

  private WatchService watchService;
  private DirectoryScannerOptions options;

  /** concurrent state */
  volatile boolean running;

  public DirectoryScanner() {
    keys = new HashMap<>(0);
  }

  @Override
  public void start(ModuleContext context) throws Exception {
    options = new DirectoryScannerOptions();
    options.setScan(true);
    context
        .unwrap(Kernel.class)
        .getScheduler()
        .getKernelExecutor()
        .submit(
            () -> {
              try {
                doRun(context.unwrap(Kernel.class));
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void stop(ModuleContext context) throws Exception {
    stop();
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

  @Override
  public void run(Map<ContextEntries, Object> ctx) {
    EntryPointRegistry registry = (EntryPointRegistry) ctx.get(ContextEntries.ENTRY_POINT_REGISTRY);
    if (registry == null) {
      log.log(Level.WARNING, "scanner.entrypoint.noregistry");
      return;
    }

    try {
      Kernel kernel = resolveKernel(registry);
      doRun(kernel);
    } catch (Exception ex) {
      running = false;
      log.log(Level.WARNING, "scanner.entrypoint.scanfailed", ex.getMessage());
    }
  }

  private Kernel resolveKernel(EntryPointRegistry registry) {
    if (Framework.isInitialized()) {
      return Framework.getInstance();
    }

    val epoints = registry.getEntryPoints(this::exportsKernel);

    if (epoints.isEmpty()) {
      log.log(Level.WARNING, "scanner.entrypoint.nokernel");
    }

    val entrypoint = epoints.iterator().next();
    log.log(Level.INFO, "scanner.entrypoint.from", entrypoint);
    return epoints.iterator().next().getService(Kernel.class);
  }

  private void doRun(Kernel kernel) throws IOException {
    val zephyr = new DefaultZephyr(kernel);
    running = true;
    doRun(kernel, zephyr);
  }

  private void doRun(Kernel kernel, Zephyr zephyr) throws IOException {
    fileSystem = kernel.getFileSystem();

    if (options != null && options.isScan()) {
      watchService = fileSystem.newWatchService();
      registerKeys(zephyr, watchService);
      watch(zephyr, watchService);
    }
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

  private void registerKeys(Zephyr zephyr, WatchService watchService) throws IOException {
    val paths = options.getDirectories();
    if (paths == null || paths.length == 0) {
      register(zephyr, fileSystem.getPath("deployments"), watchService);
      return;
    } else {
      for (val path : paths) {
        register(zephyr, Path.of(path), watchService);
      }
    }
  }

  private void register(Zephyr zephyr, Path path, WatchService watchService) throws IOException {
    log.log(Level.INFO, "scanner.watching.path", path);
    val key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    keys.put(key, path);

    if (options.isInstallOnStart()) {
      val file = path.toFile();
      val files = file.listFiles();
      if (files != null) {
        val installables =
            Arrays.stream(files)
                .map(
                    t -> {
                      try {
                        return t.getAbsoluteFile().toURI().toURL();
                      } catch (MalformedURLException ex) {
                        log.log(Level.WARNING, "scanner.directory.scan.failed", ex);
                        return null;
                      }
                    })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        zephyr.install(installables);
      }
    }
  }

  public void stop() {

    try {
      if (watchService != null) {
        watchService.close();
      }
    } catch (IOException e) {
      log.log(Level.WARNING, "scanner.watchservice.close.failed", e);
    } finally {
      running = false;
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
      handleCreate(zephyr, key, event, watchService);
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
        if (module.getSource() != null && module.getSource().is(file)) {
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
      val file = deployedFile.toAbsolutePath();
      zephyr.install(file.toFile().toURI().toURL());

      for (val module : zephyr.getPlugins()) {
        if (module.getSource() != null && module.getSource().is(file.toFile().getAbsoluteFile())) {
          zephyr.start(module.getCoordinate().toCanonicalForm());
        }
      }
    } catch (Exception ex) {
      log.log(Level.WARNING, "scanner.deployment.failed", path);
    }
  }
}
