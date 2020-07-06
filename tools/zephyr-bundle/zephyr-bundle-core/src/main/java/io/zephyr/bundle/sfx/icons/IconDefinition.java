package io.zephyr.bundle.sfx.icons;

import lombok.val;

public interface IconDefinition {

  enum Size {
    Size16px("16px", 16),
    Size24px("24px", 24),
    Size32px("32px", 32),
    Size48px("48px", 48);

    final int pixels;
    final String size;

    Size(String size, int pixels) {
      this.size = size;
      this.pixels = pixels;
    }

    public static Size fromString(String value) {
      for (val sizeEl : Size.values()) {
        if (sizeEl.size.equals(value)) {
          return sizeEl;
        }
      }
      throw new IllegalArgumentException("Error: invalid size: " + value);
    }
  }

  enum Channel {
    RGBA,
    EightBit;

    public static Channel fromString(String value) {
      val normalized = value.toLowerCase();
      switch (normalized) {
        case "rgba":
          return Channel.RGBA;
        case "8bit":
          return Channel.EightBit;
      }
      throw new IllegalStateException("Error: invalid channel: " + value);
    }
  }

  /**
   * @return the (square) size of this icon. Right now this is an enumeration instead of an integral
   *     value as it's not clear how arbitrary sizes are supported by the relevant operating systems
   */
  Size getSize();

  /**
   * @return the channel of this icon. ICNS is richer than ICO, but we're supporting the
   *     lowest-common denominator for now
   */
  Channel getChannel();
}
