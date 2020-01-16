package command.commands.plugin;

// import io.sunshower.test.common.Tests;
// import io.zephyr.kernel.Lifecycle;
// import io.zephyr.kernel.Module;
// import io.zephyr.kernel.core.KernelLifecycle;
// import io.zephyr.kernel.launch.CommandTestCase;
// import io.zephyr.kernel.launch.KernelLauncher;
// import lombok.val;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
//
// import java.io.File;
// import java.util.NoSuchElementException;
// import java.util.UUID;
//
// import static org.junit.jupiter.api.Assertions.*;


@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.JUnitAssertionsShouldIncludeMessage"})
class StartPluginCommandTest {

  //  File testPlugin1;
  //  File testPlugin2;
  //
  //  @BeforeEach
  //  void setUp() {
  //
  //    testPlugin1 =
  //        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
  //    testPlugin2 =
  //        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");
  //  }
  //
  //  @Test
  //  void ensureInstallingAndStartingSpringWorksAfterKernelRestart() throws InterruptedException {
  //    val yamlplugin =
  //        Tests.relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");
  //    val springplugin =
  //        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring", "war",
  // "libs");
  //
  //    val server = startServer();
  //
  //    try {
  //      KernelLauncher.main(
  //          new String[] {
  //            "kernel",
  //            "start",
  //            "-h",
  //            Tests.createTemp("test-2" + UUID.randomUUID()).getAbsolutePath()
  //          });
  //      waitForKernel();
  //      runRemote(
  //          "plugin",
  //          "install",
  //          testPlugin1.getAbsolutePath(),
  //          testPlugin2.getAbsolutePath(),
  //          yamlplugin.getAbsolutePath());
  //
  //      waitForPluginState(
  //          t ->
  //              t.stream()
  //                      .filter(u -> u.getLifecycle().getState() == Lifecycle.State.Resolved)
  //                      .count()
  //                  == 2);
  //      runRemote("kernel", "restart");
  //      waitForKernelState(KernelLifecycle.State.Stopped);
  //      waitForKernel();
  //
  //      runRemote("plugin", "install", springplugin.getAbsolutePath());
  //      waitForPluginState(
  //          t ->
  //              t.stream()
  //                      .filter(u -> u.getLifecycle().getState() == Lifecycle.State.Resolved)
  //                      .count()
  //                  == 3);
  //
  //      val module = moduleNamed("spring-plugin");
  //      runRemote("plugin", "start", module.getCoordinate().toCanonicalForm());
  //
  //      waitForPluginState(
  //          t ->
  //              t.stream().filter(u -> u.getLifecycle().getState() ==
  // Lifecycle.State.Active).count()
  //                  == 1);
  //
  //      assertEquals(
  //          getKernel().getModuleManager().getModules(Lifecycle.State.Active).size(),
  //          1,
  //          "must have one active plugin");
  //    } finally {
  //      KernelLauncher.main(new String[] {"kernel", "stop"});
  //      server.stop();
  //    }
  //  }
  //
  //  @Test
  //  void ensureStartingDependentPluginStartsBoth() throws InterruptedException {
  //
  //    val server = startServer();
  //    try {
  //      KernelLauncher.main(
  //          new String[] {
  //            "kernel", "start", "-h", Tests.createTemp("hello" +
  // UUID.randomUUID()).getAbsolutePath()
  //          });
  //      waitForKernel();
  //      runRemote("plugin", "install", testPlugin1.getAbsolutePath(),
  // testPlugin2.getAbsolutePath());
  //      waitForPluginCount(2);
  //      val kernel = getKernel();
  //      val module = moduleNamed("test-plugin-2");
  //      runRemote("plugin", "start", module.getCoordinate().toCanonicalForm());
  //      waitForPluginState(
  //          t ->
  //              t.stream().filter(u -> u.getLifecycle().getState() ==
  // Lifecycle.State.Active).count()
  //                  == 2);
  //
  //      val running = kernel.getModuleManager().getModules(Lifecycle.State.Active);
  //      assertEquals(running.size(), 2, "must have two running plugins");
  //
  //    } finally {
  //      KernelLauncher.main(new String[] {"kernel", "stop"});
  //      server.stop();
  //    }
  //  }
  //
  //  @Test
  //  void ensureStartingPluginWorks() throws InterruptedException {
  //
  //    val server = startServer();
  //    try {
  //      KernelLauncher.main(
  //          new String[] {
  //            "kernel", "start", "-h", Tests.createTemp("hello" +
  // UUID.randomUUID()).getAbsolutePath()
  //          });
  //      waitForKernel();
  //      runRemote("plugin", "install", testPlugin1.getAbsolutePath(),
  // testPlugin2.getAbsolutePath());
  //      waitForPluginCount(2);
  //      val kernel = getKernel();
  //      val module = moduleNamed("test-plugin-1");
  //      runRemote("plugin", "start", module.getCoordinate().toCanonicalForm());
  //      waitForPluginState(
  //          t ->
  //              t.stream().filter(u -> u.getLifecycle().getState() ==
  // Lifecycle.State.Active).count()
  //                  == 1);
  //
  //      val running = kernel.getModuleManager().getModules(Lifecycle.State.Active);
  //      assertEquals(running.size(), 1, "must have one running module");
  //
  //    } finally {
  //      KernelLauncher.main(new String[] {"kernel", "stop"});
  //      server.stop();
  //    }
  //  }
  //
  //  private Module moduleNamed(String s) {
  //    val modules = getKernel().getModuleManager().getModules();
  //    for (val module : modules) {
  //      if (module.getCoordinate().getName().equals(s)) {
  //        return module;
  //      }
  //    }
  //    throw new NoSuchElementException("No plugin named " + s);
  //  }
}
