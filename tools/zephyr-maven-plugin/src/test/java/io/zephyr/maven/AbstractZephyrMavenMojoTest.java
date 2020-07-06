package io.zephyr.maven;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;

import static org.junit.Assert.assertTrue;

public abstract class AbstractZephyrMavenMojoTest {

  protected abstract MojoRule getRule();

  protected SelfExtractingExecutableMojo getSelfExtractingExecutableMojo(String executionId)
      throws Exception {
    File pom = new File(String.format("target/test-classes/%s/", executionId));
    assertTrue(pom.exists() && pom.isDirectory());
    return (SelfExtractingExecutableMojo) getRule().lookupConfiguredMojo(pom, "generate-sfx");
  }

  protected SelfExtractingExecutableMojo getSelfExtractingExecutableMojo() throws Exception {
    File pom = new File("target/test-classes/project-to-test/");
    assertTrue(pom.exists() && pom.isDirectory());

    return (SelfExtractingExecutableMojo) getRule().lookupConfiguredMojo(pom, "generate-sfx");
  }
}
