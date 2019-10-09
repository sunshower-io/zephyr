package io.sunshower.kernel.launch;

import io.sunshower.kernel.KernelConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(KernelConfiguration.class)
@SpringBootApplication(scanBasePackages = "io.sunshower.kernel")
public class KernelLauncher {

    public static void main(String[] args) {
        SpringApplication.run(KernelLauncher.class, args);
    }

}
