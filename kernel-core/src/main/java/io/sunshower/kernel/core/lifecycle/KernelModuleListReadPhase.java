package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.module.KernelModuleEntry;
import io.sunshower.kernel.module.ModuleListParser;
import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import io.sunshower.kernel.status.Status;
import io.sunshower.kernel.status.StatusType;
import lombok.val;

public class KernelModuleListReadPhase
    extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {

  public enum EventType implements KernelProcessEvent {}

  public static final String INSTALLED_MODULE_LIST = "MODULE_LIST_INSTALLED";

  public KernelModuleListReadPhase() {
    super(EventType.class);
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    val fs = context.getKernel().getFileSystem();

    if (fs == null) {
      val status =
          new Status(
              StatusType.FAILED,
              "kernel filesystem has not been correctly created--cannot continue",
              false);
      process.addStatus(status);
      throw status.toException();
    }
    val entries = ModuleListParser.read(fs, KernelModuleEntry.MODULE_LIST);
    context.setContextValue(INSTALLED_MODULE_LIST, entries);
  }
}
