package io.sunshower.kernel.core;

import java.io.File;
import java.net.URL;
import java.util.Optional;

public interface ModuleScanner {
  Optional<ModuleDescriptor> scan(File file, URL source);
}
