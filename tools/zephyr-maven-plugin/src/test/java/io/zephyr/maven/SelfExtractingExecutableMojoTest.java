package io.zephyr.maven;

import io.zephyr.bundle.sfx.BundleOptions;
import lombok.val;
import lombok.var;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.*;

public class SelfExtractingExecutableMojoTest {

  @Rule public MojoRule rule = new MojoRule();
  private SelfExtractingExecutableMojo mojo;

  @After
  public void tearDown() throws IOException {
    deleteDirectory();
  }

  @Before
  public void setUp() throws IOException {
    deleteDirectory();
  }

  @Test
  public void ensureWorkspaceDirectoryIsSet() throws Exception {
    mojo = getSelfExtractingExecutableMojo();
    assertNotNull(mojo);
    assertNotNull(mojo.getWorkspace());

    mojo.verifyOutputDirectory();
    assertTrue(mojo.getWorkspace().exists());
  }

  @Test
  public void ensureExtractingBinaryWorks() throws Exception {
    mojo = getSelfExtractingExecutableMojo();
    mojo.execute();
    var workspace = mojo.getWorkspace();
    assertTrue(workspace.exists() && workspace.isDirectory());
    if (mojo.getPlatform() == BundleOptions.Platform.Linux) {
      val file = new File(mojo.getWorkspace(), "warp");
      assertTrue(file.exists() && file.isFile());
    }
  }

  private SelfExtractingExecutableMojo getSelfExtractingExecutableMojo() throws Exception {
    File pom = new File("target/test-classes/project-to-test/");
    assertTrue(pom.exists() && pom.isDirectory());

    return (SelfExtractingExecutableMojo) rule.lookupConfiguredMojo(pom, "generate-sfx");
  }

  private void deleteDirectory() throws IOException {
    if (mojo == null) {
      return;
    }
    val odir = mojo.getWorkspace();
    if (odir == null) {
      return;
    }

    Files.walkFileTree(
        odir.toPath(),
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }
        });
  }
}
