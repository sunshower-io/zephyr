package io.zephyr.bundle.sfx.icons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.val;

public interface IconDefinition {

  enum Size {
    Size16px("16px", 16),
    Size24px("24px", 24),
    Size32px("32px", 32),
    Size48px("48px", 48),
    Size96px("96px", 96),
    Size256px("256px", 256);

    public static Size fromString(String value) {
      for (val sizeEl : Size.values()) {
        if (sizeEl.size.equalsIgnoreCase(value)) {
          return sizeEl;
        }
      }
      throw new IllegalArgumentException("Error: invalid size: " + value);
    }

    public static List<Size> all() {
      val results = new ArrayList<>(List.of(values()));
      Collections.reverse(results);
      return Collections.unmodifiableList(results);
    }

    final int pixels;
    final String size;

    Size(String size, int pixels) {
      this.size = size;
      this.pixels = pixels;
    }

    public int getWidth() {
      return pixels;
    }

    public int getHeight() {
      return pixels;
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
