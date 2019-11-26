package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.Plugins;
import lombok.val;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * TODO: 11/26/19 Lisa: add PluginRemoveTask logging (i81n file, logger and messages, log exception
 * and throw TaskException to halt execution (Unrecoverable)
 */
public class PluginRemoveTask extends Task {

  public static final String MODULE_COORDINATE = "plugin:remove:task:coordinate";

  final Kernel kernel;

  public PluginRemoveTask(String name, Kernel kernel) {
    super(name);
    this.kernel = kernel;
  }

  @Override
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
