package io.zephyr.barometer;

import io.zephyr.api.Stoppable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import lombok.val;

final class ScanTask implements Runnable, Stoppable {
  final File watchFor;
  private volatile boolean running;
  private final ProcessIdProtocol protocol;

  public ScanTask(File watchFor, ProcessIdProtocol protocol) {
    this.watchFor = watchFor;
    this.protocol = protocol;
  }

  @Override
  public void run() {
    try {
      val watchService = getWatchService();

      if (watchFor.exists() && watchFor.isFile()) {
        protocol.alert(watchFor);
        return;
      }
      val path = watchFor.getParentFile().toPath();
      path.register(
          watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
      running = true;
      watch(watchService, path);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private void watch(WatchService watchService, Path path) throws IOException {
    WatchKey key;
    while (running) {
      try {
        key = watchService.take();
      } catch (InterruptedException e) {
        return;
      }

      for (WatchEvent<?> event : key.pollEvents()) {
        val fileName = (Path) event.context();
        val actualFile = path.resolve(fileName);
        if (actualFile.toFile().getAbsoluteFile().equals(watchFor)) {
          protocol.alert(watchFor);
          return;
        }
      }
    }
  }

  private WatchService getWatchService() throws IOException {
    return FileSystems.getDefault().newWatchService();
  }

  @Override
  public void stop() {
    running = false;
  }
}
