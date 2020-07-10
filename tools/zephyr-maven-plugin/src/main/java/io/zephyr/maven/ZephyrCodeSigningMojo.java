package io.zephyr.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "sign", defaultPhase = LifecyclePhase.PACKAGE, executionStrategy = "always")
public class ZephyrCodeSigningMojo extends AbstractZephyrMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    System.out.println(archiveBase);
  }
}
