package io.zephyr.kernel.module;

import io.zephyr.kernel.concurrency.Process;
import java.util.concurrent.CompletionStage;

public interface ModuleInstallationStatusGroup extends ModuleStatusGroup {

  CompletionStage<Process<String>> commit();

  Process<String> getProcess();
}
