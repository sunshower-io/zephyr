package io.zephyr.kernel.core.actions;

import static io.zephyr.kernel.core.Plugins.performInstallation;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.log.Logging;
import java.util.*;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings({
  "PMD.UnusedPrivateMethod",
  "PMD.UnusedFormalParameter",
  "PMD.DataflowAnomalyAnalysis"
})
public class WritePluginDescriptorPhase extends Task {
  static final Logger log = Logging.get(WritePluginDescriptorPhase.class);

  public WritePluginDescriptorPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    final Set<Module> installedPlugins =
        scope.get(ModuleInstallationCompletionPhase.INSTALLED_PLUGINS);
    val kernel = scope.<SunshowerKernel>get("SunshowerKernel");
    performInstallation(scope, installedPlugins, kernel);
    return null;
  }
}
