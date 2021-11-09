package io.zephyr.kernel;

import java.util.Comparator;
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

  private final int order;
  private final Type type;
  private final Coordinate coordinate;

  public Dependency(int order, Type type, Coordinate coordinate) {
    this.type = type;
    this.order = order;
    this.coordinate = coordinate;
  }

  public Dependency(@NonNull Type type, @NonNull Coordinate coordinate) {
    this(-1, type, coordinate);
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

  private static final class OrderComparator implements Comparator<Dependency> {

    @Override
    public int compare(Dependency o1, Dependency o2) {
      return Integer.compare(o1.order, o2.order);
    }
  }
}
