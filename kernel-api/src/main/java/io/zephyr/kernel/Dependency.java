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


  static final Comparator<Dependency> ORDER_COMPARATOR;

  static {
    ORDER_COMPARATOR = new OrderComparator();
  }

  /**
   * the type of this dependency
   */
  private final Type type;
  /**
   * specify the startup order for dependencies
   */
  private final int order;
  /**
   * the coordinate of this dependency
   */
  private final Coordinate coordinate;
  /**
   * determine whether this dependency is optional or not
   */
  private final boolean optional;
  /**
   * re-export this dependency
   */
  private final boolean reexport;
  /**
   *
   */
  private final ServicesResolutionStrategy servicesResolutionStrategy;
  /**
   * list of classes and resources that this module imports
   */
  @NonNull
  private final List<PathSpecification> imports;
  /**
   * list of classes and resources that this module imports
   */
  @NonNull
  private final List<PathSpecification> exports;

  public Dependency(
      int order,
      Type type,
      Coordinate coordinate,
      boolean optional,
      boolean export,
      ServicesResolutionStrategy servicesResolutionStrategy,
      @NonNull List<PathSpecification> imports,
      @NonNull List<PathSpecification> exports) {
    this.type = type;
    this.order = order;
    this.coordinate = coordinate;
    this.optional = optional;
    this.reexport = export;
    this.imports = imports;
    this.exports = exports;
    this.servicesResolutionStrategy = servicesResolutionStrategy;
  }

  public Dependency(
      Type type,
      Coordinate coordinate,
      boolean optional,
      boolean export,
      ServicesResolutionStrategy servicesResolutionStrategy,
      @NonNull List<PathSpecification> imports,
      @NonNull List<PathSpecification> exports) {
    this(0, type, coordinate, optional, export, servicesResolutionStrategy, imports, exports);
  }

  public Dependency(Dependency.Type type, Coordinate coordinate) {
    this(type, coordinate, false, true, ServicesResolutionStrategy.None, Collections.emptyList(),
        Collections.emptyList());
  }

  public static Comparator<Dependency> orderComparator() {
    return ORDER_COMPARATOR;
  }

  @Override
  public int compareTo(Dependency o) {
    return coordinate.compareTo(o.coordinate);
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
