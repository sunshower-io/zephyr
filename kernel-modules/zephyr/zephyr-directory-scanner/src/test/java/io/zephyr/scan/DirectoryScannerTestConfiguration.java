package io.zephyr.scan;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPointRegistry;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectoryScannerTestConfiguration {

  @Bean
  public MockEntryPoint entryPoint(Kernel kernel) {
    return new MockEntryPoint(kernel);
  }

  @Bean
  public DirectoryScanner testPluginActivator() {
    return new DirectoryScanner();
  }

  @Bean
  public EntryPointRegistry entryPointRegistry(MockEntryPoint entryPoint) {
    val reg = new MockEndpointRegistry();
    reg.addEntryPoint(entryPoint);
    return reg;
  }

  @Bean
  public Map<EntryPoint.ContextEntries, Object> contextEntries(EntryPointRegistry registry) {
    val result = new HashMap<EntryPoint.ContextEntries, Object>();
    result.put(EntryPoint.ContextEntries.ENTRY_POINT_REGISTRY, registry);
    return result;
  }
}
