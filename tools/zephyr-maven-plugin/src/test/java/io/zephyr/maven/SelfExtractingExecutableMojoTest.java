package io.zephyr.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class SelfExtractingExecutableMojoTest {

  @Rule public MojoRule rule = new MojoRule();

  @Test
  public void ensureWorkspaceDirectoryIsSet() throws Exception {
    File pom = new File("target/test-classes/project-to-test/");
    assertTrue(pom.exists() && pom.isDirectory());

    SelfExtractingExecutableMojo mojo =
        (SelfExtractingExecutableMojo) rule.lookupConfiguredMojo(pom, "generate-sfx");
    assertNotNull(mojo);
    assertNotNull(mojo.getOutputDirectory());
    assertFalse(mojo.getOutputDirectory().exists());

    mojo.verifyOutputDirectory();
    assertTrue(mojo.getOutputDirectory().exists());
  }

  @Test
  public void ensureExtractingBinaryWorks() {

  }
}
