package io.sunshower.kernel.modules;

import net.openhft.chronicle.values.MaxUtf8Length;

public interface IndexEntry {

  /** @return where this compressed entry is in its parent */
  String getPrefix();

  void setPrefix(@MaxUtf8Length(256) String prefix);

  String getName();

  void setName(@MaxUtf8Length(128) String name);
}
