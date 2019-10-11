package io.sunshower.kernel.modules;

import static java.lang.String.format;

import io.sunshower.kernel.KernelException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import net.openhft.chronicle.map.ChronicleMap;

/** Indexes are stored under workspace/plugins/<plugin-file-name>/ */
public class OffHeapClassPathIndexer {

  @NonNull private final File indexFile;
  @NonNull private final File pluginFile;
  @NonNull private final Set<String> indexedPrefixes;
  @NonNull private final ExecutorService executorService;

  public OffHeapClassPathIndexer(
      @NonNull File indexFile, @NonNull File pluginFile, @NonNull ExecutorService executorService) {
    this(indexFile, pluginFile, Collections.emptySet(), executorService);
  }

  public OffHeapClassPathIndexer(
      @NonNull File indexFile,
      @NonNull File pluginFile,
      @NonNull Set<String> indexedPrefixes,
      @NonNull ExecutorService executorService) {
    this.indexFile = indexFile;
    this.pluginFile = pluginFile;
    this.executorService = executorService;
    if (indexedPrefixes.isEmpty()) {
      indexedPrefixes = new HashSet<>();
      indexedPrefixes.add("WEB-INF/lib");
    }
    this.indexedPrefixes = indexedPrefixes;
  }

  public ClassIndex index(boolean reindex) {

    if (reindex) {
      clearIndex();
    }
    try {
      val map =
          ChronicleMap.of(String.class, String.class)
              .name(pluginFile.getName())
              .averageKeySize(100)
              .averageValueSize(120)
              .entries(100)
              .entriesPerSegment(100)
              .createPersistedTo(indexFile);
      return doIndex(map);
    } catch (IOException ex) {
      throw new KernelException(ex);
    }
  }

  private ClassIndex doIndex(ChronicleMap<String, String> map) throws IOException {
    final Map<String, IndexTask> toIndex = new HashMap<>();
    return indexPrefixed(toIndex, map);
  }

  private ClassIndex indexPrefixed(Map<String, IndexTask> toIndex, ChronicleMap<String, String> map)
      throws IOException {
    val jarFile = new JarFile(pluginFile);
    val iter = jarFile.entries();
    while (iter.hasMoreElements()) {
      val next = iter.nextElement();
      val name = next.getName();
      if (!next.isDirectory() && name.endsWith(".jar")) {
        for (val prefix : indexedPrefixes) {
          if (name.startsWith(prefix)) {
            toIndex.put(name, new IndexTask(jarFile, prefix, name, next, map));
          }
        }
      }
    }
    return concurrentlyIndex(toIndex, map);
  }

  private ClassIndex concurrentlyIndex(
      Map<String, IndexTask> toIndex, ChronicleMap<String, String> map) {
    val latch = new CountDownLatch(toIndex.size());
    for (val task : toIndex.values()) {
      executorService.submit(wrap(task, latch));
    }
    try {
      latch.await();
      System.out.println(indexFile.getAbsolutePath());
      System.out.println(indexFile.exists());
      System.out.println(map.size());
      for (Map.Entry<String, String> e : map.entrySet()) {
        System.out.println(e);
      }
      map.close();
    } catch (InterruptedException e) {

    }
    return null;
  }

  private Callable<Void> wrap(IndexTask task, CountDownLatch latch) {
    return new LatchAwareWrapper(task, latch);
  }

  @AllArgsConstructor
  static class LatchAwareWrapper implements Callable<Void> {
    final IndexTask task;
    final CountDownLatch latch;

    @Override
    public Void call() throws Exception {
      try {
        return task.call();
      } finally {
        latch.countDown();
      }
    }
  }

  private void clearIndex() {
    if (indexFile.exists() && !indexFile.delete()) {
      throw new IllegalStateException(
          format(
              "Error: Failed to delete classpath index file '%s'.  " + "Can't continue",
              indexFile.getAbsolutePath()));
    }
    try {
      if (!indexFile.createNewFile()) {
        if (!indexFile.exists()) {
          throw new IllegalStateException(
              "Failed to create new index file: " + indexFile.getAbsolutePath());
        }
      }
    } catch (IOException ex) {
      throw new IllegalStateException(
          "Failed to create new index file: " + indexFile.getAbsolutePath());
    }
  }

  static class OffHeapClassIndex implements ClassIndex {

    @Override
    public JarEntry getEntry(String className) {
      //      return JarFile/
      return null;
    }
  }

  static class IndexTask implements Callable<Void> {

    private final JarFile file;
    private final String prefix;
    private final String name;
    private final JarEntry subfile;
    private final ChronicleMap<String, String> map;

    public IndexTask(
        JarFile file,
        String prefix,
        String name, // full name of zip-entry
        JarEntry subfile,
        ChronicleMap<String, String> map) {
      this.file = file;
      this.prefix = prefix;
      this.name = name;
      this.subfile = subfile;
      this.map = map;
    }

    @Override
    public Void call() throws Exception {

      try (val is = new JarInputStream(file.getInputStream(subfile))) {
        val name = subfile.getName().substring(prefix.length() + 1);
        JarEntry entry = null;
        while ((entry = is.getNextJarEntry()) != null) {
          if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
            map.put(entry.getName().replaceAll("/", "."), name);
          }
        }
      }
      return null;
    }
  }
}
