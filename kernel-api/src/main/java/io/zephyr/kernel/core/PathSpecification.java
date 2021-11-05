package io.zephyr.kernel.core;

import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

@ToString
@EqualsAndHashCode
public class PathSpecification {

  @Getter private final Mode mode;
  @Getter private final String path;

  public PathSpecification(@NonNull Mode mode, @NonNull String path) {
    this.mode = mode;
    this.path = path;
  }

  public enum Mode {
    /** include class specification */
    Class,
    /** include glob path specification */
    All,
    /** include exactly these paths */
    Just;

    public static Mode parse(@NonNull String value) {
      val v = value.toLowerCase(Locale.ROOT).trim();
      switch (value) {
        case "all":
          return All;
        case "just":
          return Just;
        case "class":
          return Class;
        default:
          throw new IllegalArgumentException("No path specification mode: " + value);
      }
    }
  }
}
