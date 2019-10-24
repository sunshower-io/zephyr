package io.sunshower.kernel.core;

import io.sunshower.kernel.Coordinate;
import java.io.File;
import java.net.URL;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class ModuleDescriptor {

  public static final class Attributes {
    public static final String NAME = "name";
    public static final String GROUP = "group";
    public static final String ORDER = "order";
    public static final String VERSION = "version";
    public static final String DEPENDENCIES = "dependencies";
    public static final String DESCRIPTION = "description";
  }

  @NonNull private final URL source;
  @NonNull private final Integer order;
  @NonNull private final File moduleFile;
  @NonNull private final Coordinate coordinate;
  @NonNull private final List<Coordinate> dependencies;
  private final String description;
}
