package io.zephyr.barometer;

import io.zephyr.api.Stoppable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

/**
 * When Barometer starts up, it needs to save its process ID to ZEPHYR_HOME/(me).pid. It then begins
 * watching ZEPHYR_HOME for ZEPHYR_HOME/(you).pid
 */
public class ProcessIdProtocol {
  static final Logger log = Logger.getLogger(ProcessIdProtocol.class.getName());

  private Stoppable task;
  private final File zephyrHome;
  private final String agentProcessFile;
  private final String zephyrProcessFile;
  private final ExecutorService executorService;
  private final ProcessIdListener processIdListener;

  public ProcessIdProtocol(
      final File zephyrHome,
      final String zephyrProcessFile,
      final String agentProcessFile,
      final ProcessIdListener listener,
      final ExecutorService executorService) {
    this.zephyrHome = zephyrHome;
    this.executorService = executorService;
    this.agentProcessFile = agentProcessFile;
    this.zephyrProcessFile = zephyrProcessFile;
    this.processIdListener = listener;
    checkHome();
  }

  public void execute() {
    writeSelfId();
    scanForAgentId();
  }

  private void scanForAgentId() {

    val agentFile = new File(zephyrHome, agentProcessFile);
    executorService.submit((Runnable) (task = new ScanTask(agentFile, this)));
  }

  private void writeSelfId() {
    log.log(Level.INFO, "Writing current process id...");
    val pid = ProcessHandle.current().pid();
    log.log(Level.INFO, "Current process ID: {0}", pid);

    val file = new File(zephyrHome, zephyrProcessFile);
    writeId(pid, file);
  }

  private void writeId(long pid, File file) {
    if (file.exists()) {
      log.log(
          Level.INFO,
          "A process file at <" + file.getAbsolutePath() + "> already exists.  Overwriting value");
    }

    try (val fhandle =
        new PrintWriter(
            Files.newOutputStream(
                file.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE))) {
      fhandle.write(Long.toString(pid));
      fhandle.flush();
    } catch (IOException e) {
      log.log(
          Level.WARNING,
          "Failed to open file <{0}>.  Reason: {1}",
          new Object[] {file.getAbsolutePath(), e.getMessage()});
      throw new IllegalStateException(e);
    }
  }

  private void checkHome() {
    if (zephyrHome == null) {
      log.log(Level.WARNING, "Error.  Zephyr Home was null.  Not continuing");
      throw new IllegalArgumentException("Cannot process directory <null>");
    }

    if (!zephyrHome.exists()) {
      log.log(
          Level.WARNING,
          "Error.  Zephyr Home ({0}) does not exist.  Not continuing",
          zephyrHome.getAbsoluteFile());
      throw new IllegalArgumentException(
          "Cannot process directory <" + zephyrHome.getAbsolutePath() + ">");
    }

    if (!zephyrHome.isDirectory()) {
      log.log(
          Level.WARNING,
          "Error.  Zephyr Home ({0}) exists, but is not a directory",
          zephyrHome.getAbsolutePath());
    }
  }

  void alert(File watchFor) throws IOException {
    processIdListener.onProcessIdDiscovered(read(watchFor));
  }

  private long read(File watchFor) throws IOException {
    if (!(watchFor.exists() || watchFor.isFile())) {
      throw new IllegalStateException(
          "File <" + watchFor.getAbsoluteFile() + "> does not exist or is not a file");
    }
    val string = Files.readString(watchFor.toPath());
    return Long.parseLong(string);
  }

  void write(File file, int i) {
    writeId(i, file);
  }

  public void close() {
    log.log(Level.INFO, "Process ID scanner shutting down");
    if (task != null) {
      task.stop();
      log.log(Level.INFO, "Sucessfully stopped watch task");
    }

    log.log(Level.INFO, "Removing PID files...");
    val myFile = new File(zephyrHome, zephyrProcessFile);
    if (myFile.exists() && myFile.isFile()) {
      log.log(Level.INFO, "Removing PID file {0}", myFile);
      if (!myFile.delete()) {
        log.log(Level.WARNING, "Failed to delete Zephyr PID file {0}", myFile);
      }
    }
    log.log(Level.INFO, "Successfully shut down Process ID scanner");
  }
}
