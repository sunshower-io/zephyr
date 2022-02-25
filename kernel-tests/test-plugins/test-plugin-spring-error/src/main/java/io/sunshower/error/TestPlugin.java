package io.sunshower.error;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestPlugin implements ModuleActivator {

  private ConfigurableApplicationContext context;

  @Override
  public void start(ModuleContext context) {
    System.out.println("Starting error");
    this.context = SpringApplication.run(TestPlugin.class);
  }

  @Override
  public void stop(ModuleContext context) {
    System.out.println("Stopping plugin-spring-dep");
    this.context.stop();
  }
}
