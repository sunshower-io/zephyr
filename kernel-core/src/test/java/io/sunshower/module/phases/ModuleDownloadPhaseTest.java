package io.sunshower.module.phases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.sunshower.kernel.core.DefaultModuleManager;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModuleDownloadPhaseTest {

  private File pluginFile;
  private File sunshowerHome;
  private File deployDirectory;
  private KernelOptions options;
  private FileSystem fileSystem;
  private KernelProcessContext context;
  private ModuleDownloadPhase downloadPhase;

  @BeforeEach
  void setUp() throws IOException {
    pluginFile =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");
    sunshowerHome = Tests.createTemp(".sunshower-home");
    options = new KernelOptions();
    options.setHomeDirectory(sunshowerHome);
    SunshowerKernel.setKernelOptions(options);
    val kernel = new SunshowerKernel(new DefaultModuleManager());
    fileSystem = FileSystems.newFileSystem(URI.create("droplet://deploy"), Collections.emptyMap());
    context = new KernelProcessContext(kernel);
    context.setContextValue(ModuleDownloadPhase.TARGET_DIRECTORY, fileSystem.getPath("modules"));
    context.setContextValue(ModuleDownloadPhase.DOWNLOAD_URL, pluginFile.toURI().toURL());
  }

  @AfterEach
  void tearDown() throws IOException {
    fileSystem.close();
  }

  @Test
  void ensureSuccessfulDownloadResultsInFileBeingTransferredToCorrectLocation() {
    downloadPhase = spy(new ModuleDownloadPhase());
    downloadPhase.doExecute(null, context);
    File file = context.getContextValue(ModuleDownloadPhase.DOWNLOADED_FILE);
    assertTrue(file.exists(), "File must be downloaded and exist");
  }

  @Test
  void ensureSuccessfulDownloadResultsInEventsBeingCalled() {
    downloadPhase = spy(new ModuleDownloadPhase());
    downloadPhase.doExecute(null, context);
    verify(downloadPhase, atLeastOnce()).onTransfer(any(), anyDouble());
    verify(downloadPhase, times(1)).onComplete(any());
    verify(downloadPhase, times(0)).onError(any(), any());
    verify(downloadPhase, atLeast(3)).dispatch(any());
  }
}
