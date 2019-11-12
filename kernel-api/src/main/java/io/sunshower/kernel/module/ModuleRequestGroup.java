package io.sunshower.kernel.module;

import java.util.List;

public interface ModuleRequestGroup {
  List<? extends ModuleRequest> getRequests();
}
