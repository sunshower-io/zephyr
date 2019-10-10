package io.sunshower.kernel;

import io.sunshower.kernel.graph.Dependency;
import java.util.List;

public interface KernelExtensionRegistration {

  /** @return the descriptor this registration was created from */
  KernelExtensionDescriptor getDescriptor();

  /** @return the coordinate for this extension */
  Coordinate getCoordinate();

  /**
   * @return all the dependencies for this extension Can only be called if this extension is at or
   *     past the Resolved state
   */
  List<Dependency> getDependencies();
}
