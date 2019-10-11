package io.sunshower.kernel.modules;

import net.openhft.chronicle.map.ChronicleMap;

public interface ClassPathIndexer {

  ClassIndex index(boolean reindex);

  ChronicleMap<String, String> open();

  Object getIndex();
}
