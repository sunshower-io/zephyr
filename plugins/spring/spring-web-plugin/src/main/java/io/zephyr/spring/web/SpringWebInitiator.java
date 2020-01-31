package io.zephyr.spring.web;

import io.zephyr.api.ModuleContext;
import io.zephyr.api.ModuleActivator;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

@SpringBootApplication
public class SpringWebInitiator implements ModuleActivator {
  private ConfigurableApplicationContext applicationContext;

  @Override
  public void start(ModuleContext context) {
    applicationContext =
        new SpringApplicationBuilder(SpringWebInitiator.class)
            .web(WebApplicationType.SERVLET)
            .resourceLoader(new DefaultResourceLoader(context.getModule().getClassLoader()))
            .run();
  }

  @Override
  public void stop(ModuleContext context) {
    applicationContext.close();
  }
}
