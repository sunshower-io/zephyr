package io.zephyr.spring.embedded;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import lombok.val;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class EmbeddedModuleStartupListener implements ApplicationListener<ContextRefreshedEvent> {
  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    val module = (EmbeddedModule) event.getApplicationContext().getBean(Module.class);
    val kernel = event.getApplicationContext().getBean(Kernel.class);
  }
}
