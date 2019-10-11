package io.sunshower.kernel.modules;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;

public interface ClassIndex extends Iterable<Map.Entry<String, String>>, AutoCloseable, Closeable {

  /**
   * Delete and release all resources held by this index. Rebuild() must rebuild this index after
   * destroy is called.
   *
   * <p>Attempting to destroy a closed index should throw an @IllegalStateException
   */
  void destroy();
  /** @return the number of entries in this index */
  long size();
  /** @return the size of this index in bytes */
  long getIndexSize();

  /**
   * Open this index.
   *
   * @throws IllegalStateException if this index is already open
   */
  void open();

  /** @return whether this index is open. */
  boolean isOpen();

  /**
   * Close this index. Optional for in-memory indices.
   *
   * @throws IllegalStateException if this index is closed
   */
  void close();

  /** rebuild the index. Optional */
  void rebuild();

  /** @return the file containing the index. Null if this is an in-memory index */
  Path getIndexFile();

  /**
   * the file that was indexed
   *
   * @return the indexed file
   */
  Path getIndexedFile();

  /**
   * @param className the binary name of the desired class
   * @return the class if it exists in this index, or null
   */
  JarEntry getEntry(String className) throws IOException;

  /** @return a (usually lazy) iterator over this class index */
  Iterator<Map.Entry<String, String>> iterator();
}
