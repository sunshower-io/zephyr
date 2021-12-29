package io.zephyr.breeze;

import java.util.Objects;
import lombok.Data;

@Data
public class Label implements Comparable<Label> {

  /**
   * the actual key for this label
   */
  private final String key;
  private final String displayName;


  public int hashCode() {
    return key.hashCode();
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (o instanceof Label) {
      return Objects.equals(displayName, ((Label) o).displayName);
    }
    return false;
  }


  @Override
  public int compareTo(Label o) {
    return Objects.compare(this.key, o.key, String::compareTo);
  }
}
