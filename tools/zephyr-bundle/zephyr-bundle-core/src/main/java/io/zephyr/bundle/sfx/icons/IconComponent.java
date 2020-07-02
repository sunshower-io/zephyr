package io.zephyr.bundle.sfx.icons;

import lombok.val;

public class IconComponent {

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
      switch (value) {
        case "rgba":
          return Channel.RGBA;
        case "8bit":
          return Channel.EightBit;
      }
      throw new IllegalStateException("Error: invalid channel: " + value);
    }
  }

  /** doesn't seem to be a way to provide custom mappings to enums */
  private Size _size;

  private String size;

  private String channel;
  private Channel _channel;

  public void setSize(String size) {
    this.size = size;
    this._size = Size.valueOf(size);
  }

  public Size getSize() {
    if (_size == null && size != null) {
      setSize(size);
    }
    return _size;
  }

  public void setChannel(String channel) {
    this.channel = channel;
    this._channel = Channel.valueOf(channel);
  }

  public Channel getChannel() {
    if (_channel == null && channel != null) {
      setChannel(channel);
    }
    return _channel;
  }
}
