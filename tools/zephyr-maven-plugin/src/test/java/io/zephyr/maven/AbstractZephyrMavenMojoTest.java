package io.zephyr.maven;

import java.io.File;

import junit.framework.AssertionFailedError;
import lombok.val;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.MojoRule;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

public abstract class AbstractZephyrMavenMojoTest {

  protected abstract MojoRule getRule();

  protected SelfExtractingExecutableMojo getSelfExtractingExecutableMojo(String executionId)
      throws Exception {
    File pom = new File(format("target/test-classes/%s/", executionId));
    assertTrue(pom.exists() && pom.isDirectory());
    return (SelfExtractingExecutableMojo) getRule().lookupConfiguredMojo(pom, "generate-sfx");
  }

  protected SelfExtractingExecutableMojo getSelfExtractingExecutableMojo() throws Exception {
    File pom = new File("target/test-classes/project-to-test/");
    assertTrue(pom.exists() && pom.isDirectory());

    return (SelfExtractingExecutableMojo) getRule().lookupConfiguredMojo(pom, "generate-sfx");
  }

  @SuppressWarnings("unchecked")
  protected <T extends Mojo> T resolveMojo(String file, String name, Class<T> type) {
    try {
      File pom = new File(format("target/test-classes/%s/", file));
      assertTrue(pom.exists() && pom.isDirectory());
      return (T) getRule().lookupConfiguredMojo(pom, name);
    } catch (Exception ex) {
      val result = new AssertionFailedError("Expected this to succeed, failed with");
      result.addSuppressed(ex);
      throw result;
    }
  }
}
