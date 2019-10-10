package io.sunshower.kernel;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public abstract class AbstractKernelExtension implements KernelExtension {
  @Getter @Setter protected State state;

  @Getter private final Path workspaceDirectory;
  @Getter private final Path installationDirectory;

  private final KernelExtensionRegistration registration;

  @Override
  public KernelExtensionRegistration getRegistration() {
    return registration;
  }

  @Override
  public abstract Module getModule();
}
