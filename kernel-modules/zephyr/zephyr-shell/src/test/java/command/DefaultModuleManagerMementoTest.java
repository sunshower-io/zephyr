package command;

//import io.sunshower.test.common.Tests;
//import io.zephyr.kernel.Lifecycle;
//import io.zephyr.kernel.core.KernelLifecycle;
//import io.zephyr.kernel.launch.CommandTestCase;
//import io.zephyr.kernel.launch.KernelLauncher;
//import io.zephyr.kernel.modules.cli.server.Server;
//import java.io.File;
//import java.util.UUID;
//import org.junit.jupiter.api.*;

/**
 * I guess these tests should live by the module memento, but that introduces dependency weirdness
 */
@SuppressWarnings({
  "PMD.EmptyCatchBlock",
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class DefaultModuleManagerMementoTest {

  //  private File testPlugin1;
  //  private File testPlugin2;
  //  private File yamlplugin;
  //  //  private File springplugin;
  //  private Server server;
  //
  //  @BeforeEach
  //  void setUp() {
  //
  //    retrievePlugins();
  //    server = startServer();
  //    installPlugins();
  //  }
  //
  //  @Override
  //  @AfterEach
  //  protected void tearDown() {
  //    super.tearDown();
  //    KernelLauncher.main(new String[] {"kernel", "stop"});
  //    waitForKernelState(KernelLifecycle.State.Stopped);
  //    server.stop();
  //  }
  //
  //  @Test
  //  void ensureInstallingAndStartingSpringWorksAfterKernelRestart() {}
  //
  //  private void installPlugins() {
  //
  //    KernelLauncher.main(
  //        new String[] {
  //          "kernel", "start", "-h", Tests.createTemp("test-2" + UUID.randomUUID()).getAbsolutePath()
  //        });
  //    waitForKernel();
  //    runRemote(
  //        "plugin",
  //        "install",
  //        testPlugin1.getAbsolutePath(),
  //        testPlugin2.getAbsolutePath(),
  //        yamlplugin.getAbsolutePath());
  //
  //    waitForPluginState(
  //        t ->
  //            t.stream().filter(u -> u.getLifecycle().getState() == Lifecycle.State.Resolved).count()
  //                == 2);
  //    //    runRemote("kernel", "restart");
  //    //    waitForKernelState(KernelLifecycle.State.Stopped);
  //    //    waitForKernel();
  //  }
  //
  //  private void retrievePlugins() {
  //    testPlugin1 =
  //        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
  //    testPlugin2 =
  //        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");
  //    yamlplugin =
  //        Tests.relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");
  //    //    springplugin =
  //    //        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring", "war",
  //    // "libs");
  //  }
}
