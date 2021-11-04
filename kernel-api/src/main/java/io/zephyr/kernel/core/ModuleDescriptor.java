package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.Module;
import java.io.File;
import java.lang.module.ModuleDescriptor.Exports;
import java.net.URL;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class ModuleDescriptor {

  public static final class Attributes {
    public static final String NAME = "name";
    public static final String GROUP = "group";
    public static final String ORDER = "order";
    public static final String TYPE = "type";
    public static final String VERSION = "version";
    public static final String DEPENDENCIES = "dependencies";
    public static final String DESCRIPTION = "description";
  }

  /**
   * the source of this module. May refer to a local file or a
   * remote file
   */
  @NonNull private final URL source;

  /**
   * if the topological order of the current module-graph is inadequate to correctly
   * infer start order, then an additional order may be imposed by setting this value
   */
  @NonNull private final Integer order;


  /**
   * the actual file containing the module
   */
  @NonNull private final File moduleFile;

  /**
   * the type of the module.  Kernel modules are not included in
   * any module graph--not even one with other kernel modules
   */

  @NonNull private final Module.Type type;
  /**
   * a module coordinate contains the group, name, and version of a module.  This tuple
   * must be unique across all of Zephyr
   */
  @NonNull private final Coordinate coordinate;

  /**
   * a list of module-dependencies
   */
  @NonNull private final List<Dependency> dependencies;


  /**
   * list of classes and resources that this module exports
   */
  @NonNull private final List<ExportDescriptor> exports;


  /**
   * an optional description for this module
   */
  private final String description;
}
