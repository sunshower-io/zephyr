package io.sunshower.kernel.module;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class ModuleLifecycleChangeGroup implements ModuleRequestGroup {
  private List<ModuleLifecycleChangeRequest> requests;

  public ModuleLifecycleChangeGroup(ModuleLifecycleChangeRequest... requests) {
    this.requests = new ArrayList<>(requests.length);
    for (val req : requests) {
      addRequest(req);
    }
  }

  public void addRequest(ModuleLifecycleChangeRequest request) {
    requests.add(request);
  }

  @Override
  public List<ModuleLifecycleChangeRequest> getRequests() {
    return requests;
  }
}
