package io.sunshower.spring;

import io.zephyr.PluginActivator;
import io.zephyr.PluginContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestPlugin implements PluginActivator {

  private ConfigurableApplicationContext context;

  @Override
  public void start(PluginContext context) {
    System.out.println("plugin-spring starting");
    this.context = SpringApplication.run(TestPlugin.class);
  }

  @Override
  public void stop(PluginContext context) {
    System.out.println("Stopping plugin-spring");
    this.context.stop();
  }
}
