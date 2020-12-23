package io.zephyr.maven;

import io.zephyr.bundle.sfx.signing.Algorithm;
import io.zephyr.bundle.sfx.signing.SignatureConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Parameter;

public class PlatformSignatureConfiguration implements SignatureConfiguration {

  /** return the algorithm used to sign this configuration */
  @Setter
  @Parameter(
      name = "algorithm",
      alias = "algorithm",
      property = "generate-sfx.signature.algorithm",
      defaultValue = "SHA256")
  private Algorithm algorithm;

  /** the keystore configuration used by this configuration */
  @Getter
  @Setter
  @Parameter(name = "key-store", alias = "key-store", property = "generate-sfx.signature.key-store")
  private KeystoreConfiguration keyStore;

  @Getter
  @Setter
  @Parameter(name = "program", alias = "program", property = "generate-sfx.signature.program")
  private PlatformProgramConfiguration program;

  @Override
  public Algorithm getAlgorithm() {
    return algorithm;
  }
}
