package io.zephyr.maven;

import io.zephyr.bundle.sfx.signing.Algorithm;
import io.zephyr.bundle.sfx.signing.KeyStore;
import lombok.Getter;
import lombok.val;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

@Ignore
public class ZephyrCodeSigningMojoTest extends AbstractZephyrMavenMojoTest {

  @Getter @Rule public MojoRule rule = new MojoRule();
  private SelfExtractingExecutableMojo mojo;

  @Before
  public void setUp() throws Exception {
    mojo =
        resolveMojo(
            "signature-configuration-test", "generate-sfx", SelfExtractingExecutableMojo.class);
  }

  @Test
  public void ensureMojoIsInjected() throws MojoFailureException, MojoExecutionException {
    val file = new File(mojo.getArchiveBase() + ".exe");
    assertTrue(file.exists());
  }

  @Test
  public void ensureMojoHasCorrectNonDefaultAlgorithm() throws Exception {
    val sig = mojo.getSignature();
    assertNotNull("signature must not be null", sig);
    assertEquals(Algorithm.SHA512, sig.getAlgorithm());
  }

  @Test
  public void ensureMojoHasCorrectProgramName()
      throws MojoFailureException, MojoExecutionException {
    val program = mojo.getSignature().getProgram();
    assertEquals("hello", program.getName());
  }

  @Test
  public void ensureMojoHasCorrectProgramUrl() throws MojoFailureException, MojoExecutionException {
    val program = mojo.getSignature().getProgram();
    assertEquals("www.frapper.com", program.getUrl());
  }

  @Test
  public void ensureMojoHasCorrectKeyStoreType()
      throws MojoFailureException, MojoExecutionException {
    val keystoreType = mojo.getSignature().getKeyStore().getType();
    assertEquals(keystoreType, KeyStore.Type.JKS);
  }

  @Test
  public void ensureMojoHasCorrectKeyStorePassword() {
    val ksPassword = mojo.getSignature().getKeyStore().getStorePassword();
    assertEquals("test-password", ksPassword);
  }
}
