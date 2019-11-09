package io.sunshower.kernel.module;

import java.net.URL;
import lombok.Setter;

@Setter
public class ModuleInstallationRequest implements ModuleRequest {

  private URL location;
  private ModuleLifecycle.Actions lifecycleActions;

  public URL getLocation() {
    return location;
  }
}
