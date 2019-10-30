package io.sunshower.module.phases;

import static io.sunshower.kernel.Tests.install;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import io.sunshower.kernel.process.KernelProcess;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class ModuleTransferPhaseTest extends AbstractModulePhaseTestCase {

  @Test
  void ensureModuleTransferWorksUpToTransferPhase() throws Exception {
    install("test-plugin-2", context);
    val transfer = spy(new ModuleTransferPhase());
    val process = new KernelProcess(context);
    process.addPhase(new ModuleDownloadPhase());
    process.addPhase(new ModuleScanPhase());
    process.addPhase(transfer);
    process.call();
    verify(transfer, times(1)).doExecute(any(), any());

    File assembly = context.getContextValue(ModuleTransferPhase.MODULE_ASSEMBLY_FILE);
    FileSystem fs = context.getContextValue(ModuleTransferPhase.MODULE_FILE_SYSTEM);
    assertTrue(assembly.exists(), "assembly must exist");
    val path = fs.getPath("module.droplet");
    assertEquals(
        path.toAbsolutePath(), assembly.toPath().toAbsolutePath(), "paths must be the same");
  }

  @Override
  @AfterEach
  void tearDown() throws IOException {
    super.tearDown();
    FileSystem fs = context.getContextValue(ModuleTransferPhase.MODULE_FILE_SYSTEM);
    fs.close();
  }
}
