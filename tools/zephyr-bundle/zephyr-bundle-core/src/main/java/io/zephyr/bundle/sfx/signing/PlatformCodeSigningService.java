package io.zephyr.bundle.sfx.signing;

public interface PlatformCodeSigningService extends CodeSigningService {
  /** @return the keystore associate with this service */
  KeyStore getKeyStore();

  /** @return the timestamp configuration associated with this service */
  Timestamp getTimestamp();

  /** @return */
  KeySource getKeySource();
}
