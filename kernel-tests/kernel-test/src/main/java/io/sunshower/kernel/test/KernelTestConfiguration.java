package io.sunshower.kernel.test;

import static org.mockito.Mockito.mock;

import io.sunshower.test.common.Tests;
import io.zephyr.api.ModuleActivator;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.memento.Memento;
import io.zephyr.spring.embedded.EmbeddedSpringConfiguration;
import java.io.File;
import java.nio.file.FileSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EmbeddedSpringConfiguration.class)
public class KernelTestConfiguration {

  @Bean
  public ModuleActivator testPluginActivator() {
    return mock(ModuleActivator.class);
  }

  @Bean
  public FileSystem moduleFileSystem() {
    return mock(FileSystem.class);
  }

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
    return Tests.createTemp();
  }
}
