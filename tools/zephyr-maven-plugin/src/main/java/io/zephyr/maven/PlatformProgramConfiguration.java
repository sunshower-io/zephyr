package io.zephyr.maven;

import io.zephyr.bundle.sfx.signing.ProgramDescriptor;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Parameter;

public class PlatformProgramConfiguration implements ProgramDescriptor {

  @Getter
  @Setter
  @Parameter(name = "url", alias = "url", property = "generate-sfx.program.url")
  private String url;

  @Getter
  @Setter
  @Parameter(name = "name", alias = "name", property = "generate-sfx.program.name")
  private String name;
}
