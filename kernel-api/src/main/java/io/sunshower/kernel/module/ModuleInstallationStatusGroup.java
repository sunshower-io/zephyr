package io.sunshower.kernel.module;

import io.sunshower.kernel.concurrency.Process;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class ModuleInstallationStatusGroup {
  final List<ModuleInstallationStatus> statuses;
  @Getter @Setter private Process<String> process;

  public ModuleInstallationStatusGroup() {
    this.process = process;
    this.statuses = new ArrayList<>();
  }

  public List<ModuleInstallationStatus> getInstallationStatuses() {
    return Collections.unmodifiableList(statuses);
  }

  public void addStatus(ModuleInstallationStatus status) {
    statuses.add(status);
  }
}
