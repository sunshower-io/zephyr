package io.zephyr.maven;

import io.zephyr.bundle.sfx.BundleOptions;
import lombok.val;
import lombok.var;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
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
  public void ensureArchiveDirectoryIsCorrect() throws Exception {
    mojo = getSelfExtractingExecutableMojo();
    val expected =
        Paths.get(ClassLoader.getSystemResource("./project-to-test/archive").toURI())
            .toFile()
            .getAbsoluteFile();
    val actual = mojo.getArchiveDirectory().getAbsoluteFile();
    assertEquals(actual, expected);
  }

  @Test
  public void ensureExtractingBinaryWorks() throws Exception {
    val pom = new File("target/test-classes/configuration-test");
    rule.executeMojo(pom, "generate-sfx");
  }

  private SelfExtractingExecutableMojo getConfiguredMojo() throws Exception {
    val pom = new File("target/test-classes/project-to-test/");
    val mavenProject = rule.readMavenProject(pom);
    return (SelfExtractingExecutableMojo) rule.lookupConfiguredMojo(mavenProject, "generate-sfx");
  }

  private File getRealResoucesFile() {
    try {
      val classpathRoot = Paths.get(ClassLoader.getSystemResource("./").toURI());
      return classpathRoot
          .getParent()
          .getParent()
          .resolve("src/test/resources/project-to-test/archive")
          .toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
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

    if (!odir.exists()) {
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
