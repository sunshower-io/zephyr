package io.zephyr.kernel;

import io.zephyr.kernel.core.PathSpecification;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Getter
@EqualsAndHashCode
public final class Dependency implements Comparable<Dependency> {

  static final Coordinate NULL_COORDINATE;

  static final Comparator<Dependency> ORDER_COMPARATOR;

  static final Version NULL_VERSION;

  static {
    ORDER_COMPARATOR = new OrderComparator();

    NULL_VERSION =
        new Version() {
          @Override
          public boolean satisfies(String range) {
            return false;
          }

          @Override
          public int compareTo(Version version) {
            return -1;
          }
        };
    NULL_COORDINATE =
        new Coordinate() {
          @Override
          public String getName() {
            return "null";
          }

          @Override
          public String getGroup() {
            return "null";
          }

          @Override
          public Version getVersion() {
            return NULL_VERSION;
          }

          @Override
          public boolean satisfies(String range) {
            return false;
          }

          @Override
          public int compareTo(Coordinate coordinate) {
            return 0;
          }
        };
  }

  /** the type of this dependency */
  private final Type type;
  /** specify the startup order for dependencies */
  private final int order;
  /** determine whether this dependency is optional or not */
  private final boolean optional;
  /** re-export this dependency */
  private final boolean reexport;
  /** */
  private final ServicesResolutionStrategy servicesResolutionStrategy;
  /** list of classes and resources that this module imports */
  @NonNull private final List<PathSpecification> imports;
  /** list of classes and resources that this module imports */
  @NonNull private final List<PathSpecification> exports;

  @Getter private final CoordinateSpecification coordinateSpecification;

  /** the coordinate of this dependency once it has been resolved */
  private Coordinate coordinate;

  /**
   * determine whether this dependency has been resolved (i.e. getCoordinate will not return null)
   */
  private boolean resolved;

  public Dependency(
      int order,
      Type type,
      CoordinateSpecification spec,
      boolean optional,
      boolean export,
      ServicesResolutionStrategy servicesResolutionStrategy,
      @NonNull List<PathSpecification> imports,
      @NonNull List<PathSpecification> exports) {
    this.type = type;
    this.order = order;
    this.optional = optional;
    this.reexport = export;
    this.imports = imports;
    this.exports = exports;
    this.coordinateSpecification = spec;
    this.servicesResolutionStrategy = servicesResolutionStrategy;
  }

  public Dependency(
      Type type,
      CoordinateSpecification spec,
      boolean optional,
      boolean export,
      ServicesResolutionStrategy servicesResolutionStrategy,
      @NonNull List<PathSpecification> imports,
      @NonNull List<PathSpecification> exports) {
    this(0, type, spec, optional, export, servicesResolutionStrategy, imports, exports);
  }

  public Dependency(
      Dependency.Type type, Coordinate coordinate, CoordinateSpecification specification) {
    this(
        type,
        specification,
        false,
        true,
        ServicesResolutionStrategy.None,
        Collections.emptyList(),
        Collections.emptyList());
    setCoordinate(coordinate);
  }

  public Dependency(Dependency.Type type, CoordinateSpecification specification) {
    this(type, NULL_COORDINATE, specification);
  }

  public static Comparator<Dependency> orderComparator() {
    return ORDER_COMPARATOR;
  }

  @Override
  public int compareTo(Dependency o) {
    return coordinate.compareTo(o.coordinate);
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public void setCoordinate(final Coordinate coordinate) {
    if (coordinate != null) {
      resolved = true;
    } else {
      resolved = false;
    }
    this.coordinate = coordinate;
  }

  public boolean isResolved() {
    return resolved;
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

  private static final class OrderComparator implements Comparator<Dependency> {

    @Override
    public int compare(Dependency o1, Dependency o2) {
      return Integer.compare(o1.order, o2.order);
    }
  }
}
