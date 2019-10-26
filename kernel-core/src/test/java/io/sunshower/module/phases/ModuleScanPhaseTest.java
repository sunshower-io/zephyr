package io.sunshower.module.phases;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.Module.Type;
import io.sunshower.kernel.core.ModuleDescriptor;
import io.sunshower.kernel.core.SemanticVersion;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.kernel.process.KernelProcessEvent;
import io.sunshower.kernel.process.Phase;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class ModuleScanPhaseTest extends AbstractModulePhaseTestCase {
  protected Phase<KernelProcessEvent, KernelProcessContext> phase;

  @Override
  @BeforeEach
  void setUp() throws Exception {
    super.setUp();
    phase = new ModuleScanPhase();
    val pluginFile = install("test-plugin-2").getPluginFile();
    context.setContextValue(ModuleDownloadPhase.DOWNLOADED_FILE, pluginFile);
  }

  @Test
  void ensureScanPhaseProducesResultForExistingFile() {
    phase.execute(null, context);
    ModuleDescriptor descriptor = context.getContextValue(ModuleScanPhase.MODULE_DESCRIPTOR);
    val coord = descriptor.getCoordinate();
    assertEquals(descriptor.getType(), Type.Plugin);
    assertEquals(coord.getGroup(), "sunshower.io", "must have correct group");
    assertEquals(coord.getName(), "test-plugin-2", "must have correct name");
    assertEquals(coord.getVersion(), new SemanticVersion("1.0.0-SNAPSHOT"));
  }
}
