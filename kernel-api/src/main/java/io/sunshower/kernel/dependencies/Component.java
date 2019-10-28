package io.sunshower.kernel.dependencies;

import io.sunshower.kernel.Module;
import java.util.List;

public interface Component {
  int size();

  boolean isCyclic();

  List<Module> getMembers();
}
