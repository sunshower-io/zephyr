package io.sunshower.kernel.process;

import io.sunshower.module.phases.*;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ModuleInstallProcess {

  //  static final Logger log = Logging.get(ModuleInstallProcess.class);
  //  public static final String channel = "kernel:process:install";
  //
  //  final String url;
  //  final Kernel kernel;
  //
  //  @Override
  //  public String getChannel() {
  //    return channel;
  //  }
  //
  //  @Override
  //  public void process(ConcurrentProcess process) {
  //    kernel.getScheduler().registerHandler(ModuleEntryWriteProcessor.getInstance());
  //    process.perform();
  //    //    kernel.getScheduler().registerHandler(ModuleEntryWriteProcessor.getInstance());
  //  }
  //
  //  @Override
  //  public void perform() {
  //    try {
  //      create().call();
  //    } catch (Exception e) {
  //      log.log(Level.WARNING, "module.install.failed");
  //    }
  //  }
  //
  //  KernelProcess create() {
  //    val ctx = new KernelProcessContext(kernel);
  //    val proc = new KernelProcess(ctx);
  //    proc.addPhase(new ModuleDownloadPhase());
  //    proc.addPhase(new ModuleScanPhase());
  //    proc.addPhase(new ModuleTransferPhase());
  //    proc.addPhase(new ModuleUnpackPhase());
  //    proc.addPhase(new ModuleInstallationCompletionPhase());
  //
  //    decorate(ctx);
  //    return proc;
  //  }
  //
  //  private void decorate(KernelProcessContext ctx) {
  //    try {
  //      val fs = kernel.getFileSystem();
  //      ctx.setContextValue(ModuleDownloadPhase.TARGET_DIRECTORY, fs.getPath("modules"));
  //      ctx.setContextValue(ModuleDownloadPhase.DOWNLOAD_URL, new URL(url));
  //      ctx.setContextValue(
  //          ModuleUnpackPhase.LIBRARY_DIRECTORIES, Collections.singleton("WEB-INF/lib"));
  //    } catch (MalformedURLException e) {
  //      log.log(Level.WARNING, "module.install.failed", e.getMessage());
  //    }
  //  }
}
