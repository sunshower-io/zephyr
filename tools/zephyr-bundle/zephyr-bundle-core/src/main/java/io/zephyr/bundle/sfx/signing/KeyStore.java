package io.zephyr.bundle.sfx.signing;

import java.io.File;

public interface KeyStore {

  enum Type {
    JKS,
    PKCS11,
    PKCS12
  }

  /** @return the type of the keystore */
  Type getType();

  /** @return the key store file */
  File getKeyStore();

  /** @return the key store password */
  String getKeyPassword();

  /** @return the password of the store. May be omitted if getKeyPassword() is the same */
  String getStorePassword();

  /** @return the alias of the key in the keystore */
  String getKeyAlias();
}
