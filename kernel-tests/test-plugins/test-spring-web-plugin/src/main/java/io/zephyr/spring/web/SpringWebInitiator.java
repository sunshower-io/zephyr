package io.zephyr.spring.web;

import io.zephyr.api.ModuleContext;
import io.zephyr.api.PluginActivator;
import io.zephyr.spring.web.controllers.HelloController;
import lombok.val;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

@SpringBootApplication(scanBasePackageClasses = HelloController.class)
public class SpringWebInitiator implements PluginActivator {
  private ConfigurableApplicationContext applicationContext;

  @Override
  public void start(ModuleContext context) {
    val module = context.getModule();
    applicationContext =
        new SpringApplicationBuilder(SpringWebInitiator.class)
            .web(WebApplicationType.SERVLET)
            .resourceLoader(new DefaultResourceLoader(module.getClassLoader()))
            .run();
  }

  @Override
  public void stop(ModuleContext context) {
    applicationContext.close();
  }
}
