package io.sunshower.module.phases;

import io.sunshower.kernel.process.KernelProcess;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModuleUnpackPhaseTest extends AbstractModulePhaseTestCase {

  @Test
  void ensureModuleTransferWorksUpToTransferPhase() throws Exception {
    context.setContextValue(ModuleUnpackPhase.LIBRARY_DIRECTORIES, Collections.singleton("WEB-INF/lib/"));
    val process = new KernelProcess(context);
    process.addPhase(new ModuleDownloadPhase());
    process.addPhase(new ModuleScanPhase());
    process.addPhase(new ModuleTransferPhase());
    process.addPhase(new ModuleUnpackPhase());
    process.call();
  }
}
