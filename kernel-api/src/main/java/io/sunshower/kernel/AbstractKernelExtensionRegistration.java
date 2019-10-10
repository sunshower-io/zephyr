package io.sunshower.kernel;

import io.sunshower.kernel.graph.Dependency;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class AbstractKernelExtensionRegistration implements KernelExtensionRegistration {

  @Getter @Setter private Module module;

  @Getter @Setter private Path loadDirectory;
  @Setter private List<Dependency> dependencies;
  @Getter @Setter private Path workspaceDirectory;

  @Getter final KernelExtensionDescriptor descriptor;

  @Getter @Setter private Path installationDirectory;

  @Getter @Setter @NonNull private Coordinate coordinate;

  public AbstractKernelExtensionRegistration(@NonNull KernelExtensionDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  @Override
  public List<Dependency> getDependencies() {
    if (dependencies == null) {
      return Collections.emptyList();
    }
    return dependencies;
  }
}
