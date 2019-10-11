package io.sunshower.kernel.modules;

import static java.lang.String.format;

import io.sunshower.kernel.KernelException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import lombok.*;
import net.openhft.chronicle.map.ChronicleMap;

/** Indexes are stored under workspace/plugins/<plugin-file-name>/classloader.idx */
public class OffHeapClassPathIndexer implements ClassPathIndexer {

  private static final String SEPARATOR = "::";

  /** the destination for the classpath index */
  @NonNull private final File indexFile;

  /** the file that we're going to index (typically the plugin war/jar) */
  @NonNull private final File indexedFile;

  /** which subdirectories are we going to index? */
  @NonNull private final Set<String> indexedPrefixes;

  /** For concurrent indexing, we need an executor service */
  @NonNull private final ExecutorService executorService;

  static final String INDEX_FILE_NAME = "classloader.idx";

  static final Set<String> DEFAULT_PREFIXES =
      new HashSet<>(
          Arrays.asList(
              "WEB-INF/lib")); // META-INF/WEB-INF/classes are taken care of by module class loader default load paths
  private Statistics statistics;
  private ChronicleMap<String, String> index;

  public OffHeapClassPathIndexer(
      @NonNull File toIndex,
      @NonNull File dataDirectory,
      @NonNull Set<String> indexedPrefixes,
      @NonNull ExecutorService executorService) {

    this.indexedFile = toIndex;
    this.executorService = executorService;
    if (indexedPrefixes.isEmpty()) {
      this.indexedPrefixes = DEFAULT_PREFIXES;
    } else {
      this.indexedPrefixes = indexedPrefixes;
    }
    this.indexFile = new File(dataDirectory, INDEX_FILE_NAME);
  }

  public OffHeapClassPathIndexer(
      File toIndex, File dataDirectory, ExecutorService executorService) {
    this(toIndex, dataDirectory, DEFAULT_PREFIXES, executorService);
  }

  public ClassIndex index(boolean reindex) {

    if (reindex) {
      clearIndex();
    }
    try {
      val map = new TreeMap<String, String>();
      val statisics = new Statistics(indexFile);
      return doIndex(map, statisics);
    } catch (IOException ex) {
      throw new KernelException(ex);
    }
  }

  @Override
  public ChronicleMap<String, String> open() {
    if (statistics == null) {
      throw new IllegalStateException(
          "Error--cannot open this index without rebuilding it (try calling index(true) first)");
    }
    try {
      return statistics.createBackingIndex();
    } catch (IOException ex) {
      throw new ClassPathIndexException(ex);
    }
  }

  @Override
  public Object getIndex() {
    return index;
  }

  private ClassIndex doIndex(Map<String, String> map, Statistics statisics) throws IOException {
    final Map<String, IndexTask> toIndex = new HashMap<>();
    return indexPrefixed(toIndex, map, statisics);
  }

  private ClassIndex indexPrefixed(
      Map<String, IndexTask> toIndex, Map<String, String> map, Statistics statistics)
      throws IOException {
    val jarFile = new JarFile(indexedFile);
    val iter = jarFile.entries();
    while (iter.hasMoreElements()) {
      val next = iter.nextElement();
      val name = next.getName();
      if (!next.isDirectory()) {
        for (val prefix : indexedPrefixes) {
          if (name.startsWith(prefix)) {
            toIndex.put(name, new IndexTask(statistics, jarFile, prefix, name, next, map));
          }
        }
      }
    }

    return concurrentlyIndex(toIndex, map, statistics);
  }

  private ClassIndex concurrentlyIndex(
      Map<String, IndexTask> toIndex, Map<String, String> map, Statistics statistics) {
    val latch = new CountDownLatch(toIndex.size());
    for (val task : toIndex.values()) {
      executorService.submit(wrap(task, latch));
    }

    try {
      latch.await();
      val idx = statistics.createBackingIndex();
      idx.putAll(map);
      this.statistics = statistics;
      this.index = idx;
      return new OffHeapSortedCompactClassIndex(this, indexFile, indexedFile, idx);
    } catch (InterruptedException | IOException e) {
      throw new ClassPathIndexException(e);
    }
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

  static class IndexTask implements Callable<Void> {

    private final JarFile file;
    private final String prefix;
    private final String name;
    private final JarEntry subfile;
    private final Map<String, String> map;
    private final Statistics statistics;

    public IndexTask(
        Statistics statistics,
        JarFile file,
        String prefix,
        String name, // full name of zip-entry
        JarEntry subfile,
        Map<String, String> map) {
      this.statistics = statistics;
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
        JarEntry entry;
        statistics.addFile();
        statistics.computeValue(name);
        while ((entry = is.getNextJarEntry()) != null) {
          if (!entry.isDirectory()) {
            val key = entry.getName().replaceAll("/", ".");
            statistics.addEntry();
            statistics.computeKey(key);

            map.put(key, encode(name, prefix));
          }
        }
      }
      return null;
    }

    String encode(String prefix, String value) {
      return prefix + OffHeapClassPathIndexer.SEPARATOR + value;
    }
  }

  @ToString
  static final class Statistics {
    final File indexFile;
    final AtomicLong keyLength;
    final AtomicLong valueLength;
    final AtomicInteger fileCount;
    final AtomicInteger entryCount;

    Statistics(File indexFile) {
      this.indexFile = indexFile;
      keyLength = new AtomicLong();
      valueLength = new AtomicLong();
      fileCount = new AtomicInteger();
      entryCount = new AtomicInteger();
    }

    void computeKey(String key) {
      if (key != null) {
        keyLength.addAndGet(key.length());
      }
    }

    void computeValue(String value) {
      if (value != null) {
        valueLength.addAndGet(value.length());
      }
    }

    int addFile() {
      return fileCount.incrementAndGet();
    }

    int addEntry() {
      return entryCount.incrementAndGet();
    }

    ChronicleMap<String, String> createBackingIndex() throws IOException {
      val fcount = fileCount.get();
      val ecount = entryCount.get();

      val avgKeyLength = keyLength.get() / ecount;
      val avgValLength = valueLength.get() / fcount;

      return ChronicleMap.of(String.class, String.class)
          .name(indexFile.getName())
          .averageKeySize(avgKeyLength)
          .averageValueSize(avgValLength)
          .entries(ecount)
          .createPersistedTo(indexFile);
    }
  }
}
