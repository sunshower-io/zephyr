package io.sunshower.kernel.configuration;

import io.sunshower.kernel.api.services.KernelService;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RootApplicationContextConfiguration implements ApplicationContextInitializer {

    public RootApplicationContextConfiguration() {
        System.out.println("Initialized");
    }


    @Bean
    public KernelService start() {
        System.out.println("New Kernel");
        return new KernelService();
    }

    @Bean
    public String sayHello(KernelService service) {
        return service.toString();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

    }
}
