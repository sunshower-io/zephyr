package io.zephyr.aire.core;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

@SpringBootApplication
public class AireModule implements ModuleActivator {
  private ConfigurableApplicationContext applicationContext;

  @Override
  public void start(ModuleContext context) {
    applicationContext =
        new SpringApplicationBuilder(AireModule.class)
            .web(WebApplicationType.SERVLET)
            .resourceLoader(new DefaultResourceLoader(context.getModule().getClassLoader()))
            .run();
  }

  @Override
  public void stop(ModuleContext context) {
    applicationContext.stop();
  }

  public static void main(String[] args) {
    SpringApplication.run(AireModule.class, args);
  }
}
