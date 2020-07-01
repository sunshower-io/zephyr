package io.zephyr.maven;

import lombok.Getter;
import lombok.val;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ExecutionConfigurationTest extends AbstractZephyrMavenMojoTest {

  @Getter @Rule public MojoRule rule = new MojoRule();

  @Test
  public void ensureMojoConfigurationExists() throws Exception {
    val cfgs = getSelfExtractingExecutableMojo("single-configuration-project").getExecutableConfiguration();
//    assertNotNull("Configurations must not be null", cfgs);
    System.out.println(cfgs);
  }
}
