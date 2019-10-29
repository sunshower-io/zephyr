package io.sunshower.kernel.core;

import io.sunshower.kernel.Source;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ModuleSource implements Source {
  @Getter final URI location;
}
