package io.zephyr.spring.web;

import io.zephyr.PluginActivator;
import io.zephyr.PluginContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringWebInitiator implements PluginActivator {
  private ConfigurableApplicationContext applicationContext;

  @Override
  public void start(PluginContext context) {
    applicationContext = SpringApplication.run(SpringWebInitiator.class);
  }

  @Override
  public void stop(PluginContext context) {
    applicationContext.close();
  }
}
