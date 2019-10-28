package io.sunshower.spring;

import io.sunshower.kernel.core.ModuleActivator;
import io.sunshower.kernel.core.ModuleContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestPlugin implements ModuleActivator {

  @Bean
  public String sayHello() {
    return "Hello!";
  }

  public static void main(String[] args) {}

  @Override
  public void onLifecycleChanged(ModuleContext context) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(TestPlugin.class);
    ctx.refresh();
    String s = (String) ctx.getBean("sayHello");
    System.out.println(s);
  }
}
