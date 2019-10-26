package io.sunshower.module.phases;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.process.KernelProcess;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Collections;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class ModuleInstallationCompletionPhaseTest extends AbstractModulePhaseTestCase {

  private KernelProcess process;

  @Override
  @BeforeEach
  void setUp() throws Exception {
    super.setUp();
    process = new KernelProcess(context);
    process.addPhase(new ModuleDownloadPhase());
    process.addPhase(new ModuleScanPhase());
    process.addPhase(new ModuleTransferPhase());
    process.addPhase(new ModuleUnpackPhase());
    process.addPhase(new ModuleInstallationCompletionPhase());
  }

  @Test
  void ensureModuleTransferWorksUpToTransferPhase() throws Exception {
    install("test-plugin-2");
    context.setContextValue(
        ModuleUnpackPhase.LIBRARY_DIRECTORIES, Collections.singleton("WEB-INF/lib/"));
    process.call();
    val modules = kernel.getModuleManager().getModules(Module.Type.Plugin);
    assertFalse(modules.isEmpty(), "module must be installed");
  }

  @Override
  @AfterEach
  void tearDown() throws IOException {
    super.tearDown();
    FileSystem fs = context.getContextValue(ModuleTransferPhase.MODULE_FILE_SYSTEM);
    fs.close();
  }
}
