package io.zephyr.spring.web;

import io.zephyr.PluginActivator;
import io.zephyr.PluginContext;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

@SpringBootApplication
public class SpringWebInitiator implements PluginActivator {
  private ConfigurableApplicationContext applicationContext;

  @Override
  public void start(PluginContext context) {
    applicationContext =
        new SpringApplicationBuilder(SpringWebInitiator.class)
            .web(WebApplicationType.SERVLET)
            .resourceLoader(new DefaultResourceLoader(context.getModule().getClassLoader()))
            .run();
  }

  @Override
  public void stop(PluginContext context) {
    applicationContext.close();
  }
}
