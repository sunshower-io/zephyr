package io.zephyr.bundle.sfx.signing;

public interface ProgramDescriptor {

  /** @return the program name to embed */
  String getName();

  /** @return the program URL to embed */
  String getUrl();
}
