package io.sunshower.kernel.core.actions;

import io.sunshower.common.io.Files;
import io.sunshower.common.io.MonitorableChannels;
import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.log.Logging;
import java.io.File;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import lombok.val;

/** Downloads a file from a URL into a temp directory */
@SuppressWarnings("PMD.UnusedFormalParameter")
public class ModuleDownloadPhase extends Task implements ChannelTransferListener {

  static final Logger log = Logging.get(ModuleDownloadPhase.class);
  static final ResourceBundle bundle;

  static {
    bundle = log.getResourceBundle();
  }

  private final ThreadLocal<File> targetFile;
  public static final String DOWNLOAD_URL = "MODULE_DOWNLOAD_URL";
  public static final String DOWNLOADED_FILE = "DOWNLOADED_MODULE_FILE";
  public static final String TARGET_DIRECTORY = "MODULE_TARGET_DIRECTORY";

  /** */
  public ModuleDownloadPhase(String name) {
    super(name);
    targetFile = new ThreadLocal<>();
  }

  @Override
  public Task.TaskValue run(Scope scope) {
    URL downloadUrl = (URL) parameters().get(DOWNLOAD_URL);
    scope.set(DOWNLOAD_URL, downloadUrl);
    Path moduleDirectory = scope.get(TARGET_DIRECTORY);
    downloadModule(downloadUrl, moduleDirectory, scope);
    return null;
  }

  @Override
  public void onTransfer(ReadableByteChannel channel, double progress) {}

  @Override
  public void onComplete(ReadableByteChannel channel) {
    targetFile.remove();
  }

  @Override
  public void onError(ReadableByteChannel channel, Exception ex) {}

  @Override
  public void onCancel(ReadableByteChannel channel) {}

  @AllArgsConstructor
  public static class TransferData {
    final double progress;
  }

  private void downloadModule(URL downloadUrl, Path moduleDirectory, Scope context) {
    val targetDirectory = getTargetDirectory(moduleDirectory, context);
    val targetFile = new File(targetDirectory, Files.getFileName(downloadUrl));
    this.targetFile.set(targetFile);
    log.log(Level.INFO, "module.download.beforestart", new Object[] {downloadUrl, targetDirectory});
    doTransfer(downloadUrl, targetFile, context);
  }

  private File getTargetDirectory(Path moduleDirectory, Scope context) {
    val targetDirectory = moduleDirectory.toFile();
    if (!targetDirectory.exists()) {
      log.log(Level.INFO, "module.download.targetdir.creating", targetDirectory);
      if (!(targetDirectory.exists() || targetDirectory.mkdirs())) {
        throw new TaskException(TaskStatus.UNRECOVERABLE);
      }
    }
    return targetDirectory;
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void doTransfer(URL downloadUrl, File targetFile, Scope context) {
    try {
      val transfer = MonitorableChannels.transfer(downloadUrl, targetFile);
      transfer.addListener(this);
      transfer.call();
      context.set(DOWNLOADED_FILE, targetFile);
    } catch (Exception ex) {
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
  }
}
