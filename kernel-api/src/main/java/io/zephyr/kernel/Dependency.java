package io.zephyr.kernel;

import io.zephyr.kernel.core.ExportDescriptor;
import io.zephyr.kernel.core.ImportDescriptor;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public final class Dependency {

  /**
   * the type of this dependency
   */
  final Type type;
  /**
   * the coordinate of this dependency
   */
  final Coordinate coordinate;

  /**
   * determine whether this dependency is optional or not
   */
  final boolean optional;

  /**
   * re-export this dependency
   */
  final boolean export;

  /**
   * list of classes and resources that this module imports
   */
  @NonNull private final List<ImportDescriptor> imports;


  /**
   * list of classes and resources that this module imports
   */
  @NonNull private final List<ExportDescriptor> exports;
  /**
   */
  final ServicesResolutionStrategy servicesResolutionStrategy;


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


  public enum ServicesResolutionStrategy {
    None,
    Import,
    Export;

    public static ServicesResolutionStrategy parse(@NonNull String type) {
      val normalized = type.toLowerCase(Locale.ROOT).trim();
      switch (normalized) {
        case "none":
          return None;
        case "import":
          return Import;
        case "export":
          return Export;
      }
      throw new IllegalArgumentException("Unknown service resolution strategy: " + normalized);
    }
  }
}
