package io.zephyr.kernel.extensions;

import java.util.Comparator;

public interface PrioritizedExtension extends Comparable<PrioritizedExtension> {

  int LOWEST_PRIORITY = Integer.MAX_VALUE;
  int HIGHEST_PRIORITY = Integer.MIN_VALUE;

  int getPriority();

  default int compareTo(PrioritizedExtension extension) {
    if (extension == null) {
      throw new IllegalStateException("Somehow was passed a null prioritized extension");
    }
    return Integer.compare(getPriority(), extension.getPriority());
  }

  static <T extends PrioritizedExtension> Comparator<? super T> getComparator() {
    return PrioritizedExtension::compareTo;
  }
}
