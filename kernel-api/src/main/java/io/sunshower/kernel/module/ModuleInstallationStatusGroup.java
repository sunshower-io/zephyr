package io.sunshower.kernel.module;

import io.sunshower.kernel.concurrency.Process;
import java.util.concurrent.CompletionStage;

public interface ModuleInstallationStatusGroup extends ModuleStatusGroup {

  CompletionStage<Process<String>> commit();

  Process<String> getProcess();
}
