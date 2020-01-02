package io.zephyr.spring.web;

import io.zephyr.PluginActivator;
import io.zephyr.PluginContext;
import io.zephyr.kernel.Module;
import io.zephyr.spring.web.controllers.HelloController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackageClasses = HelloController.class)
public class SpringWebInitiator implements PluginActivator {
  private ConfigurableApplicationContext applicationContext;

  public static void main(String[] args) {
    //    val app =
    //        new SpringApplication(
    //            new DefaultResourceLoader(SpringWebInitiator.class.getClassLoader()),
    //            SpringWebInitiator.class,
    //            HelloController.class);
    //    app.run();
  }

  @Override
  public void start(PluginContext context, Module module) {
    //    applicationContext =
    //        new SpringApplicationBuilder(SpringWebInitiator.class)
    //            .web(WebApplicationType.SERVLET)
    //            .resourceLoader(new DefaultResourceLoader(module.getClassLoader()))
    //            .run();
  }

  @Override
  public void stop(PluginContext context, Module module) {
    //    applicationContext.close();
  }
}
