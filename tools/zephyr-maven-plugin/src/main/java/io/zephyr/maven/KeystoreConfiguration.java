package io.zephyr.maven;

import io.zephyr.bundle.sfx.signing.KeyStore;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public class KeystoreConfiguration implements KeyStore {
  @Getter
  @Setter
  @Parameter(name = "type", alias = "type", property = "generate-sfx.signature.key-store.type")
  private Type type;

  @Getter
  @Setter
  @Parameter(
      name = "store-password",
      alias = "store-password",
      property = "generate-sfx.signature.key-store.store-password")
  private String storePassword;

  @Getter
  @Setter
  @Parameter(
      name = "key-password",
      alias = "key-password",
      property = "generate-sfx.signature.key-store.key-password")
  private String keyPassword;

  @Override
  public File getKeyStore() {
    return null;
  }

  @Override
  public String getKeyAlias() {
    return null;
  }
}
