package io.sunshower.kernel.dependencies;

import java.util.List;

public interface Components {

  boolean hasCycle();

  List<Component> getCycles();
}
