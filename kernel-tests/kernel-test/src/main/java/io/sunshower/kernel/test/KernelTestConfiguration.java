package io.sunshower.kernel.test;

import static org.mockito.Mockito.mock;

import io.sunshower.test.common.Tests;
import io.zephyr.api.ModuleActivator;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelModuleLoader;
import io.zephyr.kernel.core.ModuleClasspath;
import io.zephyr.kernel.core.ModuleClasspathManager;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.core.ModuleDescriptor;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.memento.Memento;
import io.zephyr.spring.embedded.EmbeddedModuleClasspath;
import io.zephyr.spring.embedded.EmbeddedModuleLoader;
import io.zephyr.spring.embedded.EmbeddedSpringConfiguration;
import java.io.File;
import java.util.Collections;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EmbeddedSpringConfiguration.class)
public class KernelTestConfiguration {

  @Bean
  public ModuleClasspath moduleClasspath() {
    return new EmbeddedModuleClasspath(
        new EmbeddedModuleLoader(ClassLoader.getSystemClassLoader()));
  }

  @Bean
  public ModuleDescriptor descriptor() {
    val location = getClass().getProtectionDomain().getCodeSource().getLocation();
    val coord = ModuleCoordinate.create("test", "test", "1.0.0-SNAPSHOT");
    return new ModuleDescriptor(
        location,
        0,
        new File(location.getFile()),
        Module.Type.Plugin,
        coord,
        Collections.emptyList(),
        Collections.emptyList(),
        "test plugin");
  }

  @Bean
  public ModuleClasspathManager moduleClasspathManager(DependencyGraph graph, Kernel kernel) {
    val result = new KernelModuleLoader(graph, kernel);
    ((SunshowerKernel) kernel).setModuleClasspathManager(result);
    return result;
  }

  @Bean
  public ModuleActivator testPluginActivator() {
    return mock(ModuleActivator.class);
  }

  //  @Bean
  //  public FileSystem moduleFileSystem() {
  //    return mock(FileSystem.class);
  //  }
  //
  @Bean
  public Memento memento() {
    return mock(Memento.class);
  }

  @Bean
  public ModuleLifecycleManager moduleLifecycleManager(final Zephyr zephyr) {
    return new ModuleLifecycleManager(zephyr);
  }

  @Bean
  public File kernelRootDirectory() {
    val temp = Tests.createTemp();
    val options = new KernelOptions();
    options.setHomeDirectory(temp);
    return temp;
  }
}
