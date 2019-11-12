package io.sunshower.kernel.module;

import io.sunshower.kernel.concurrency.Process;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface ModuleStatusGroup {
  CompletionStage<Process<String>> commit();

  Process<String> getProcess();

  Set<? extends ModuleRequest> getRequests();
}
