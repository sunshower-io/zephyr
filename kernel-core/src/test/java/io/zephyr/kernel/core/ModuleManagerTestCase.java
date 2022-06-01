package io.zephyr.kernel.core;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

@Log
@DisabledIfEnvironmentVariable(
    named = "BUILD_ENVIRONMENT",
    matches = "github",
    disabledReason = "RMI is flaky")
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisabledOnOs(OS.WINDOWS)
public class ModuleManagerTestCase {

  protected Kernel kernel;
  protected ModuleManager manager;
  protected Scheduler<String> scheduler;
  protected SunshowerKernelConfiguration cfg;
  protected File plugin1;
  protected File plugin2;
  ModuleInstallationRequest req2;
  ModuleInstallationRequest req1;

  @BeforeEach
  protected void setUp() throws Exception {

    val tempdir = Tests.createTemp();
    val options = new KernelOptions();

    options.setHomeDirectory(tempdir);

    SunshowerKernel.setKernelOptions(options);

    cfg =
        DaggerSunshowerKernelConfiguration.factory()
            .create(options, ClassLoader.getSystemClassLoader());
    kernel = cfg.kernel();
    manager = kernel.getModuleManager();
    manager.initialize(kernel);
    scheduler = kernel.getScheduler();
    kernel.start();
    assertTrue(manager.getModules().isEmpty());

    plugin1 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
    plugin2 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");

    req1 = new ModuleInstallationRequest();
    req1.setLifecycleActions(ModuleLifecycle.Actions.Install);
    req1.setLocation(plugin1.toURI().toURL());

    req2 = new ModuleInstallationRequest();
    req2.setLifecycleActions(ModuleLifecycle.Actions.Activate);
    req2.setLocation(plugin2.toURI().toURL());
  }

  protected File moduleIn(String location) {
    val l = String.format("kernel-tests:test-plugins:%s", location);
    return Tests.relativeToProjectBuild(l, "war", "libs");
  }

  @SneakyThrows
  protected void install(File... files) {
    val group = new ModuleInstallationGroup();
    for (val file : files) {
      val request = new ModuleInstallationRequest();
      request.setLocation(file.toURI().toURL());
      group.add(request);
    }
    manager.prepare(group).commit().toCompletableFuture().get();
  }

  @AfterEach
  void tearDown() throws Exception {
    kernel.stop();
  }

  private File configureFiles() {
    return Tests.createTemp();
  }

  @SneakyThrows
  protected void start(String s) {
    request(s, ModuleLifecycle.Actions.Activate);
  }

  @SneakyThrows
  private void request(String pluginName, ModuleLifecycle.Actions action) {

    val plugin = find(pluginName);
    val lifecycleRequest = new ModuleLifecycleChangeRequest(plugin.getCoordinate(), action);
    val grp = new ModuleLifecycleChangeGroup(lifecycleRequest);
    manager.prepare(grp).commit().toCompletableFuture().get();
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  protected <T> Class<T> findClass(String moduleName, String className) {
    val module = find(moduleName);
    val classloader = module.getClassLoader();
    return (Class<T>) classloader.loadClass(className);
  }

  protected Module find(String name) {
    log.log(Level.INFO, "Attempting to locate any module named: {0}", name);
    log.log(Level.INFO, "Available modules: ");
    val modules = manager.getModules();
    for (val module : modules) {
      log.log(Level.INFO, "\t ''{0}''", module.getCoordinate().getName());
    }

    try {
      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(
              () ->
                  manager.getModules().stream()
                      .anyMatch(t -> t.getCoordinate().getName().contains(name)));
    } catch (Exception ex) {
      log.log(Level.SEVERE, "No module named ''{0}'' found.  Available modules:");
      for (val module : manager.getModules()) {
        log.log(Level.SEVERE, "\t ''{0}''", module.getCoordinate().getName());
      }
      throw ex;
    }
    return manager.getModules().stream()
        .filter(t -> t.getCoordinate().getName().contains(name))
        .findFirst()
        .get();
  }

  @SneakyThrows
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected <T> T invokeClassOn(
      String moduleName, String typeName, String methodName, Object... args) {
    val module = find(moduleName);
    val type = module.getClassLoader().loadClass(typeName);
    val instance = type.getConstructor().newInstance();

    val method = type.getMethod(methodName, argTypesFor(args));
    method.trySetAccessible();
    return (T) method.invoke(instance, args);
  }

  @SneakyThrows
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected <T> T invokeServiceOn(
      String name, String servicetype, String methodName, Object... args) {
    val manager = kernel.getModuleManager();
    val module =
        manager.getModules().stream()
            .filter(t -> t.getCoordinate().getName().equals(name))
            .findFirst()
            .orElseThrow();
    await().atMost(10, TimeUnit.SECONDS).until(() -> module.getContext() != null);
    val ref = module.getContext().getReferences(servicetype).get(0);
    val service = ref.getDefinition().get();
    val type = ref.getDefinition().getType().getMethod(methodName, argTypesFor(args));
    return (T) type.invoke(service, args);
  }

  protected Class<?>[] argTypesFor(Object[] args) {

    val result = new Class<?>[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = args[i].getClass();
    }
    return result;
  }
}
