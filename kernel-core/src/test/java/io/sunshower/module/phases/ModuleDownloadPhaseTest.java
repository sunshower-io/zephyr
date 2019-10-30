package io.sunshower.module.phases;

import static io.sunshower.kernel.Tests.install;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.MalformedURLException;
import org.junit.jupiter.api.Test;

class ModuleDownloadPhaseTest extends AbstractModulePhaseTestCase {

  ModuleDownloadPhase downloadPhase;

  @Test
  void ensureSuccessfulDownloadResultsInFileBeingTransferredToCorrectLocation()
      throws MalformedURLException {
    install("test-plugin-2", context);
    downloadPhase = spy(new ModuleDownloadPhase());
    downloadPhase.doExecute(null, context);
    File file = context.getContextValue(ModuleDownloadPhase.DOWNLOADED_FILE);
    assertTrue(file.exists(), "File must be downloaded and exist");
  }

  @Test
  void ensureSuccessfulDownloadResultsInEventsBeingCalled() throws MalformedURLException {
    install("test-plugin-2", context);
    downloadPhase = spy(new ModuleDownloadPhase());
    downloadPhase.doExecute(null, context);
    verify(downloadPhase, atLeastOnce()).onTransfer(any(), anyDouble());
    verify(downloadPhase, times(1)).onComplete(any());
    verify(downloadPhase, times(0)).onError(any(), any());
    verify(downloadPhase, atLeast(3)).dispatch(any());
  }
}
