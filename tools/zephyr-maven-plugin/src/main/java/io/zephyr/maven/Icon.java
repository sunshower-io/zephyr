package io.zephyr.maven;

import io.zephyr.bundle.sfx.icons.IconDefinition;
import org.apache.maven.plugins.annotations.Parameter;

public class Icon implements IconDefinition {

  /** doesn't seem to be a way to provide custom mappings to enums */
  private Size _size;

  private Channel _channel;

  @Parameter(name = "size", alias = "size", property = "generate-sfx.size")
  private String size;

  @Parameter(name = "channel", alias = "channel", property = "generate-sfx.channel")
  private String channel;

  public void setSize(String size) {
    this.size = size;
    this._size = Size.fromString(size);
  }

  public Size getSize() {
    if (_size == null && size != null) {
      setSize(size);
    }
    return _size;
  }

  public void setChannel(String channel) {
    this.channel = channel;
    this._channel = Channel.fromString(channel);
  }

  public Channel getChannel() {
    if (_channel == null && channel != null) {
      setChannel(channel);
    }
    return _channel;
  }
}
