package io.zephyr.maven;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.maven.plugins.annotations.Parameter;

@ToString
public class ExecutableConfiguration {

  @Getter @Setter @Parameter private String version;
}
