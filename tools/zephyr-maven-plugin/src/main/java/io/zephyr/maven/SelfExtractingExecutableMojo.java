package io.zephyr.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "generate-sfx", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class SelfExtractingExecutableMojo extends AbstractMojo {

  @Parameter(
      required = true,
      property = "workspace",
      defaultValue = "${project.build.directory}/sfx-workspace")
  private File outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {}
}
