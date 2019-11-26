package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.Plugins;
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

  static final Logger log = Logger.getLogger(PluginRemoveTask.class.getName());

  public static final String MODULE_COORDINATE = "plugin:remove:task:coordinate";

  final Kernel kernel;

  public PluginRemoveTask(String name, Kernel kernel) {
    super(name);
    this.kernel = kernel;
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public TaskValue run(Scope scope) {
    synchronized (this) {
      try {
        final Coordinate coordinate = scope.get(MODULE_COORDINATE);
        val module = kernel.getModuleManager().getModule(coordinate);
        val fs = Plugins.getFileSystem(coordinate);
        val visitor = new DeleteVisitor();
        for (val path : fs.getSnd().getRootDirectories()) {
          Files.walkFileTree(path, visitor);
        }
        kernel.getModuleManager().getDependencyGraph().remove(module);
        kernel.getModuleClasspathManager().uninstall(module);
      } catch (IOException ex) {
        log.log(Level.WARNING, "plugin.remove.failed", new Object[] {ex.getMessage()});
        log.log(Level.FINE, "Error", ex);
        throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
      }
      return null;
    }
  }

  static final class DeleteVisitor extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      Files.delete(dir);
      return FileVisitResult.CONTINUE;
    }
  }
}
