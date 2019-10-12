package io.sunshower.kernel.modules;

import io.sunshower.common.io.IO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.val;
import net.openhft.chronicle.map.ChronicleMap;

public class OffHeapSortedCompactClassIndex implements ClassIndex {

  volatile boolean closed;

  final File indexFile;
  final File indexedFile;
  final ClassPathIndexer indexer;

  private volatile JarFile jarFile;

  private final Object lock = new Object[0];

  private ChronicleMap<String, String> index;

  public OffHeapSortedCompactClassIndex(
      ClassPathIndexer indexer,
      File indexFile,
      File indexedFile,
      ChronicleMap<String, String> idx) {
    this.index = idx;
    this.indexer = indexer;
    this.indexFile = indexFile;
    this.indexedFile = indexedFile;
  }

  @Override
  public void destroy() {
    close();
    deleteIndexFile();
  }

  @Override
  public long size() {
    checkClosed();
    return index.longSize();
  }

  @Override
  public long getIndexSize() {
    checkClosed();
    return indexFile.getTotalSpace();
  }

  @Override
  public void open() {
    if (closed) {
      if (indexFile.exists() && getIndexSize() > 0) {
        this.index = indexer.open();
        closed = false;
      } else {
        throw new IllegalStateException("Corrupted index");
      }
    } else {
      throw new IllegalStateException("Index is already open");
    }
  }

  @Override
  public boolean isOpen() {
    return !closed;
  }

  @Override
  public void close() {
    if (closed) {
      throw new IllegalStateException("Index is already closed");
    }
    index.close();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void rebuild() {
    deleteIndexFile();
    indexer.index(true);
    this.index = (ChronicleMap<String, String>) indexer.getIndex();
  }

  @Override
  public Path getIndexFile() {
    return indexFile.toPath();
  }

  @Override
  public Path getIndexedFile() {
    return indexedFile.toPath();
  }

  private EntryWithStream getEntryWithStream(String className, boolean openStream)
      throws IOException {
    val indexEntry = index.get(className);
    if (indexEntry == null) {
      return null;
    }

    val segments = indexEntry.split("::");
    if (segments.length != 2) {
      throw new IllegalArgumentException("Bad index entry: " + indexEntry);
    }
    val jarfile = getJarFile();
    val file = segments[0];
    val location = segments[1];
    val pathName = normalizeBinaryName(className);
    return doRead(jarfile, pathName, file, location, openStream);
  }

  @Override
  public EntryWithStream getEntryWithStream(String fileName) throws IOException {
    return getEntryWithStream(fileName, true);
  }

  @Override
  public JarEntry getEntry(String className) throws IOException {
    val a = getEntryWithStream(className, false);
    if (a != null) {
      return a.entry;
    }
    return null;
    //    return getEntryWithStream(className, false).entry;
  }

  private String normalizeBinaryName(String className) {
    if (className.endsWith(".class")) {
      val len = ".class".length();
      val pathPart = className.substring(0, className.length() - len);
      return pathPart.replaceAll("\\.", "/") + ".class";
    } else {
      return className;
    }
  }

  public static void check(InputStream compressedInput, String name) throws IOException {
    ZipInputStream input = new ZipInputStream(compressedInput);
    ZipEntry entry = null;
    while ((entry = input.getNextEntry()) != null) {
      System.out.println("Found " + entry.getName() + " in " + name);
      if (entry.getName().endsWith(".zip")) { // TODO Better checking
        check(input, name + "/" + entry.getName());
      }
    }
  }

  private EntryWithStream doRead(
      JarFile jarfile, String pathName, String file, String location, boolean openStream) {
    val path = location + "/" + file;

    var subfile = jarfile.getJarEntry(path);
    if (subfile == null) {
      throw new IllegalArgumentException("Bad index entry: " + path);
    }
    try (val inputStream = new JarInputStream(jarfile.getInputStream(subfile))) {
      JarEntry entry;
      while ((entry = inputStream.getNextJarEntry()) != null) {
        if (entry.getName().equals(pathName)) {
          if (!openStream) {
            return new EntryWithStream(entry, null);
          } else {
            return new EntryWithStream(entry, IO.copyStream(inputStream, entry));
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    throw new NoSuchElementException("Couldn't find " + pathName + " in " + path);
  }

  private JarFile getJarFile() throws IOException {
    var jarfile = jarFile;
    if (jarfile == null) {
      synchronized (lock) {
        synchronized (lock) {
          jarfile = jarFile;
          if (jarfile == null) {
            jarFile = jarfile = new JarFile(indexedFile);
          }
        }
      }
    }
    return jarfile;
  }

  @Override
  public Iterator<Map.Entry<String, String>> iterator() {
    checkClosed();
    return index.entrySet().iterator();
  }

  @Override
  public String normalize(String key) {
    return normalizeBinaryName(key);
  }

  @Override
  public String getPath(String key) {
    String result;
    if (key.endsWith(".class")) {
      result = key.substring(0, key.length() - ".class".length());
    } else {
      result = key;
    }
    return result.substring(0, result.lastIndexOf('/'));
  }

  private void checkClosed() {
    if (closed) {
      throw new IllegalStateException("Cannot perform operation on closed index");
    }
  }

  private void deleteIndexFile() {
    if (!indexFile.delete()) {
      throw new ClassPathIndexException("Failed to delete index file: " + indexFile);
    }
  }
}
