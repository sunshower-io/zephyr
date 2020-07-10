package io.zephyr.maven;

import lombok.Getter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

public class ZephyrCodeSigningMojoTest extends AbstractZephyrMavenMojoTest {

  @Getter @Rule public MojoRule rule = new MojoRule();
  private ZephyrCodeSigningMojo mojo;

  @Before
  public void setUp() throws Exception {
    mojo = resolveMojo("single-configuration-project", "sign", ZephyrCodeSigningMojo.class);
  }

  @Test
  public void ensureMojoIsInjected() throws MojoFailureException, MojoExecutionException {
    System.out.println(mojo);
    mojo.execute();
  }
}
