package io.zephyr.maven;

import lombok.Getter;
import lombok.val;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExecutionConfigurationTest extends AbstractZephyrMavenMojoTest {

  @Getter @Rule public MojoRule rule = new MojoRule();
  private ExecutableConfiguration executableConfiguration;

  @Before
  public void setUp() throws Exception {
    executableConfiguration =
        getSelfExtractingExecutableMojo("single-configuration-project")
            .getExecutableConfiguration();
  }

  @Test
  public void ensureVersionStringIsCorrect() {
    assertEquals(executableConfiguration.getVersionString(), "1.0.1");
  }

  @Test
  public void ensureFileVersionIsCorrect() {
    assertEquals(executableConfiguration.getFileVersion(), "1.0.2");
  }

  @Test
  public void ensureProductVersionIsCorrect() {
    assertEquals(executableConfiguration.getProductVersion(), "1.0.6");
  }

  @Test
  public void ensureManifestFileIsCorrect() {
    assertTrue("File must exist", executableConfiguration.getManifestFile().exists());
  }

  @Test
  public void ensureResourceStringsAreCorrect() {
    val resourceStrings = executableConfiguration.getResourceStrings();
    assertEquals("must have 2 elements", 2, resourceStrings.size());
    assertEquals("must have test-resource", "whatever1", resourceStrings.get("test-resource"));
    assertEquals("must have test-resource2", "whatever2", resourceStrings.get("test-resource2"));
  }

  @Test
  public void ensureDefinitionExists() {
    val definition = executableConfiguration.getIconDefinition();
    assertTrue(definition.getSource().exists());
  }

  @Test
  public void ensureDefinitionHasCorrectNumberOfIcons() {
    val definition = executableConfiguration.getIconDefinition().getIcons();
    assertEquals(6, definition.size());
  }
}
