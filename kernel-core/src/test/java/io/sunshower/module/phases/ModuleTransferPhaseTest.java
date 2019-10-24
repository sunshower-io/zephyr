package io.sunshower.module.phases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import io.sunshower.kernel.process.KernelProcess;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileSystem;

class ModuleTransferPhaseTest extends AbstractModulePhaseTestCase {

  @Test
  void ensureModuleTransferWorksUpToTransferPhase() throws Exception {
    val transfer = spy(new ModuleTransferPhase());
    val process = new KernelProcess(context);
    process.addPhase(new ModuleDownloadPhase());
    process.addPhase(new ModuleScanPhase());
    process.addPhase(transfer);
    process.call();
    verify(transfer, times(1)).doExecute(any(), any());

    File assembly = context.getContextValue(ModuleTransferPhase.MODULE_ASSEMBLY);
    FileSystem fs = context.getContextValue(ModuleTransferPhase.MODULE_FILE_SYSTEM);
    assertTrue(assembly.exists(), "assembly must exist");
    val path = fs.getPath("module.droplet");
    assertEquals(
        path.toAbsolutePath(), assembly.toPath().toAbsolutePath(), "paths must be the same");
  }
}
