package io.sunshower.kernel.osgi;

import io.sunshower.kernel.KernelExtensionDescriptor;
import java.util.Comparator;

public class SourceSortingExtensionComparator<U extends KernelExtensionDescriptor>
    implements Comparator<U> {

  @Override
  public int compare(U lhs, U rhs) {
    return lhs.toString().compareTo(rhs.getSource().toString());
  }
}
