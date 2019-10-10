package io.sunshower.kernel;

import java.net.URL;
import java.nio.file.Path;
import lombok.*;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AbstractKernelExtensionDescriptor implements KernelExtensionDescriptor {

  @Getter @NonNull private final URL source;

  @Getter @NonNull private final Path loadedFile;

  @Getter @NonNull private final Path loadDirectory;
}
