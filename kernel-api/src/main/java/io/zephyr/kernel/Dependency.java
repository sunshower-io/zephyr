package io.zephyr.kernel;

import io.zephyr.kernel.core.ExportDescriptor;
import io.zephyr.kernel.core.ImportDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Getter
@EqualsAndHashCode
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
   *
   */
  final ServicesResolutionStrategy servicesResolutionStrategy;
  /**
   * list of classes and resources that this module imports
   */
  @NonNull
  private final List<ImportDescriptor> imports;
  /**
   * list of classes and resources that this module imports
   */
  @NonNull
  private final List<ExportDescriptor> exports;
  public Dependency(Type type, Coordinate coordinate, boolean optional, boolean export,
      ServicesResolutionStrategy servicesResolutionStrategy,
      @NonNull List<ImportDescriptor> imports,
      @NonNull List<ExportDescriptor> exports) {
    this.type = type;
    this.coordinate = coordinate;
    this.optional = optional;
    this.export = export;
    this.servicesResolutionStrategy = servicesResolutionStrategy;
    this.imports = imports;
    this.exports = exports;
  }


  public Dependency(Dependency.Type type, Coordinate coordinate) {
    this.type = type;
    this.export = true;
    this.optional = false;
    this.coordinate = coordinate;
    this.exports = Collections.emptyList();
    this.imports = Collections.emptyList();
    this.servicesResolutionStrategy = ServicesResolutionStrategy.None;
  }

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
