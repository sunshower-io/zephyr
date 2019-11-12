package io.sunshower.spring;

import io.sunshower.PluginActivator;
import io.sunshower.PluginContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestPlugin implements PluginActivator {

  private ConfigurableApplicationContext context;

  @Override
  public void start(PluginContext context) {
    this.context = SpringApplication.run(TestPlugin.class);
  }

  @Override
  public void stop(PluginContext context) {
    System.out.println("Stopping plugin-spring-dep");
    this.context.stop();
  }
}
