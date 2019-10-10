package io.sunshower.kernel;

import static com.github.resource4j.objects.parsers.ResourceParsers.propertyMap;
import static com.github.resource4j.objects.providers.ResourceObjectProviders.bind;
import static com.github.resource4j.objects.providers.ResourceObjectProviders.patternMatching;
import static com.github.resource4j.resources.BundleFormat.format;
import static com.github.resource4j.resources.ResourcesConfigurationBuilder.configure;

import com.github.resource4j.resources.RefreshableResourcesConfigurator;
import com.github.resource4j.spring.SpringResourceObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KernelConfiguration {

  @Bean
  public RefreshableResourcesConfigurator resourcesConfiguration(
      SpringResourceObjectProvider springResourceObjects) {
    return configure()
        .sources(
            patternMatching()
                .when(".+\\.properties$", bind(springResourceObjects).to("classpath:/i18n"))
                .otherwise(bind(springResourceObjects).to("classpath:/templates")))
        .formats(format(propertyMap(), ".properties"))
        .get();
  }
}
