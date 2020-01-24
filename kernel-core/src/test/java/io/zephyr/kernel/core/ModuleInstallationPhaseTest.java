package io.zephyr.kernel.core;

//import io.sunshower.kernel.process.KernelProcess;
//import io.sunshower.kernel.process.KernelProcessContext;
//import io.sunshower.module.phases.*;
//import lombok.val;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

public class ModuleInstallationPhaseTest {

  //  private KernelProcessContext context;
  //  private DefaultModuleManager moduleManager;
  //  private ExecutorService service;
  //
  //  @BeforeEach
  //  void setUp() {
  //    service = Executors.newFixedThreadPool(2);
  //    context = new KernelProcessContext();
  //    moduleManager = new DefaultModuleManager();
  //  }
  //
  //  @Test
  //  void ensureModuleManagerPhaseAPIMakesSense() throws InterruptedException {
  //    val installationProcess = new KernelProcess(context);
  //    installationProcess.addPhase(new ModuleDownloadPhase());
  //    val unpackPhase = new ModuleUnpackPhase();
  //
  //    unpackPhase.addPhase(new ModuleScanPhase()); // reads downloaded file for
  //    unpackPhase.addPhase(
  //        new ModuleTransferPhase()); // copies from kernel/temp/modules/whatever.whatever to <plugin
  //    // folder>/module.droplet
  //    unpackPhase.addPhase(new ModuleIndexPhase());
  //    installationProcess.addPhase(unpackPhase);
  //
  //    installationProcess.registerListener(
  //        ModuleDownloadPhase.EventType.OnDownloadStarted,
  //        new ModuleDownloadPhase.ModuleDownloadEventListener() {});
  //
  //    service.submit(installationProcess);
  //    Thread.sleep(100);
  //  }
}
