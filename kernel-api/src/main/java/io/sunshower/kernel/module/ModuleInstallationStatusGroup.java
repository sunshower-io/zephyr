package io.sunshower.kernel.module;

import io.sunshower.kernel.concurrency.Process;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.sunshower.kernel.concurrency.Scheduler;
import io.sunshower.kernel.concurrency.TaskTracker;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

public interface ModuleInstallationStatusGroup {

  CompletionStage<String> commit();

  Process<String> getProcess();
}
