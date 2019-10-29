package io.sunshower.module.phases;

import static java.text.MessageFormat.format;

import io.sunshower.common.io.Files;
import io.sunshower.common.io.MonitorableChannels;
import io.sunshower.kernel.events.Event;
import io.sunshower.kernel.events.EventListener;
import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import io.sunshower.kernel.status.Status;
import io.sunshower.kernel.status.StatusType;
import java.io.File;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.val;

/** Downloads a file from a URL into a temp directory */
public class ModuleDownloadPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext>
    implements ChannelTransferListener {

  static final Logger log = Logging.get(ModuleDownloadPhase.class);
  static final ResourceBundle bundle;

  static {
    bundle = log.getResourceBundle();
  }

  public static final String DOWNLOAD_URL = "MODULE_DOWNLOAD_URL";
  public static final String DOWNLOADED_FILE = "DOWNLOADED_MODULE_FILE";
  public static final String TARGET_DIRECTORY = "MODULE_TARGET_DIRECTORY";

  public enum EventType implements KernelProcessEvent {
    OnDownloadStarted,
    OnDownloadProgressed,
    OnDownloadComplete,
    OnDownloadError
  }

  public static class ModuleDownloadEvent
      implements Event<KernelProcessEvent, KernelProcessContext> {

    private final Object data;
    private final EventType type;

    public ModuleDownloadEvent(EventType type, Object data) {
      this.type = type;
      this.data = data;
    }

    @Override
    public KernelProcessEvent getType() {
      return type;
    }
  }

  public static class ModuleDownloadEventListener
      implements EventListener<KernelProcessEvent, KernelProcessContext> {

    @Override
    public void onEvent(Event<KernelProcessEvent, KernelProcessContext> event) {}
  }

  private final ThreadLocal<File> targetFile;

  /** */
  public ModuleDownloadPhase() {
    super(EventType.class);
    targetFile = new ThreadLocal<>();
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    URL downloadUrl = context.getContextValue(DOWNLOAD_URL);
    Path moduleDirectory = context.getContextValue(TARGET_DIRECTORY);
    downloadModule(downloadUrl, moduleDirectory, context);
  }

  @Override
  public void onTransfer(ReadableByteChannel channel, double progress) {
    dispatch(new ModuleDownloadEvent(EventType.OnDownloadProgressed, new TransferData(progress)));
  }

  @Override
  public void onComplete(ReadableByteChannel channel) {
    dispatch(new ModuleDownloadEvent(EventType.OnDownloadComplete, targetFile.get()));
    targetFile.remove();
  }

  @Override
  public void onError(ReadableByteChannel channel, Exception ex) {
    dispatch(new ModuleDownloadEvent(EventType.OnDownloadError, ex));
  }

  @Override
  public void onCancel(ReadableByteChannel channel) {}

  @AllArgsConstructor
  public static class TransferData {
    final double progress;
  }

  private void downloadModule(URL downloadUrl, Path moduleDirectory, KernelProcessContext context) {
    val targetDirectory = getTargetDirectory(moduleDirectory, context);
    val targetFile = new File(targetDirectory, Files.getFileName(downloadUrl));
    this.targetFile.set(targetFile);
    log.log(Level.INFO, "module.download.beforestart", new Object[] {downloadUrl, targetDirectory});
    doTransfer(downloadUrl, targetFile, context);
  }

  private File getTargetDirectory(Path moduleDirectory, KernelProcessContext context) {
    val targetDirectory = moduleDirectory.toFile();
    if (!targetDirectory.exists()) {
      log.log(Level.INFO, "module.download.targetdir.creating", targetDirectory);
      if (!targetDirectory.mkdirs()) {
        val status =
            new Status(
                StatusType.FAILED,
                format("module.download.targetdir.create.failed", this, targetDirectory),
                false);
        context.getKernel().getModuleManager().addStatus(status);
        throw new PhaseException(Phase.State.Unrecoverable, this, status.toException());
      }
    }
    return targetDirectory;
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void doTransfer(URL downloadUrl, File targetFile, KernelProcessContext context) {
    try {
      val transfer = MonitorableChannels.transfer(downloadUrl, targetFile);
      transfer.addListener(this);
      dispatch(new ModuleDownloadEvent(EventType.OnDownloadStarted, targetFile));
      transfer.call();
      context.setContextValue(DOWNLOADED_FILE, targetFile);
    } catch (Exception ex) {
      throw new PhaseException(Phase.State.Unrecoverable, this, ex);
    }
  }
}
