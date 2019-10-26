package io.sunshower.kernel;

import lombok.*;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public final class Dependency {

  public enum Type {
    Library,
    Service;

    public static Type parse(@NonNull String p) {
      val normalized = p.toLowerCase().trim();
      switch (normalized) {
        case "library":
          return Library;
        case "service":
          return Service;
      }
      throw new IllegalArgumentException("Unknown dependency type: " + p);
    }
  }

  final Type type;
  final Coordinate coordinate;
}
