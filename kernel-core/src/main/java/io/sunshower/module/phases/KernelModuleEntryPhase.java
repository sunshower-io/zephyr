package io.sunshower.module.phases;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.module.KernelModuleEntry;
import io.sunshower.kernel.module.ModuleEntryWrite;
import io.sunshower.kernel.process.AbstractPhase;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.kernel.process.KernelProcessEvent;
import io.sunshower.kernel.process.Process;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import lombok.val;

public class KernelModuleEntryPhase
    extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {

  static final Logger log = Logging.get(KernelModuleEntryPhase.class);

  protected KernelModuleEntryPhase(Class<? extends KernelProcessEvent> type) {
    super(type);
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    Module module = context.getContextValue(ModuleInstallationCompletionPhase.INSTALLED_MODULE);
    if (module.getType() == Module.Type.KernelModule) {
      log.log(Level.INFO, "module.type.kernel.begin", module.getCoordinate());
      val fileSystem = FileSystems.getFileSystem(URI.create("droplet://kernel"));
      val path = fileSystem.getPath(KernelModuleEntry.MODULE_LIST);
      context.getKernel().scheduleTask(new ModuleEntryWrite(path, module));
    } else {
      log.log(Level.INFO, "module.type.plugin", module.getCoordinate());
    }
  }
}
