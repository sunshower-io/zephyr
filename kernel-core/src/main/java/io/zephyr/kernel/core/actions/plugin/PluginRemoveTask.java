package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.api.ModuleEvents;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.Events;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class PluginRemoveTask extends Task {

  public static final String MODULE_COORDINATE = "plugin:remove:task:coordinate";
  static final Logger log = Logger.getLogger(PluginRemoveTask.class.getName());
  final Kernel kernel;

  public PluginRemoveTask(String name, Kernel kernel) {
    super(name);
    this.kernel = kernel;
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public TaskValue run(Scope scope) {
    final Coordinate coordinate = scope.get(MODULE_COORDINATE);
    val module = kernel.getModuleManager().getModule(coordinate);
    kernel.dispatchEvent(ModuleEvents.REMOVING, Events.create(module));
    val moduleName = coordinate.getName();
    log.log(Level.INFO, "plugin.remove.starting", new Object[] {moduleName});
    try {
      val fs = module.getFileSystem();
      val visitor = new DeleteVisitor();
      for (val path : fs.getRootDirectories()) {
        Files.walkFileTree(path, visitor);
      }
      kernel.getModuleManager().getDependencyGraph().remove(module);
      kernel.getModuleClasspathManager().uninstall(module);
    } catch (IOException ex) {
      log.log(Level.WARNING, "plugin.remove.failed", new Object[] {moduleName, ex.getMessage()});
      log.log(Level.WARNING, "Error", ex);
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    kernel.dispatchEvent(ModuleEvents.REMOVED, Events.create(module));
    log.log(Level.INFO, "plugin.remove.succeeded", new Object[] {moduleName});
    return null;
  }

  static final class DeleteVisitor extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      try {
        Files.delete(file);
      } catch (IOException ex) {
        log.log(
            Level.WARNING,
            "Failed to delete file ''{0}''.  Reason: ''{0}''.  Will attempt to delete upon Zephyr process exit",
            new Object[] {file, ex.getMessage()});
        file.toFile().deleteOnExit();
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      try {
        Files.delete(dir);
      } catch (IOException ex) {
        log.log(
            Level.WARNING,
            "Failed to delete directory ''{0}''.  Reason: ''{0}''.  Will attempt to delete upon Zephyr process exit",
            new Object[] {dir, ex.getMessage()});
        dir.toFile().deleteOnExit();
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
