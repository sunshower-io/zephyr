package io.sunshower.kernel.fs;

import java.nio.file.FileSystem;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.val;

@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.UseVarargs"})
public class FileSystemRegistry implements Iterable<FileSystem> {

  private static final Pattern keyPattern = Pattern.compile("\\.");

  private int registrySize;
  final Entry root = new Entry(null);

  public FileSystem add(String[] segments, FileSystem fileSystem) {
    val current = lookup(segments, true);
    val existing = current.value;
    current.value = fileSystem;
    registrySize++;
    return existing;
  }

  public FileSystem add(@NonNull String key, @NonNull FileSystem fileSystem) {
    val segments = keyPattern.split(key);
    return add(segments, fileSystem);
  }

  public int size() {
    return registrySize;
  }

  @SuppressWarnings("PMD.NullAssignment")
  public FileSystem remove(String[] segments) {
    Entry current = root;
    Entry previous = null;
    for (val segment : segments) {
      val entries = current.children;

      Entry found = null;
      for (val child : entries) {
        if (segment.equals(child.name)) {
          found = child;
          break;
        }
      }

      if (found == null) {
        return null;
      } else {
        previous = current;
        current = found;
      }
    }

    val result = current.value;
    current.value = null;

    if (previous != null) {
      previous.children.remove(current);
      if (previous.children.isEmpty()) {
        ((ArrayList) previous.children).trimToSize();
      }
    }
    registrySize--;
    return result;
  }

  public FileSystem remove(@NonNull String key) {
    val segments = keyPattern.split(key);
    return remove(segments);
  }

  public boolean contains(String[] key) {
    return get(key) != null;
  }

  public boolean contains(String key) {
    return get(key) != null;
  }

  public FileSystem get(@NonNull String key) {
    val segments = keyPattern.split(key);
    return get(segments);
  }

  public FileSystem get(String[] segments) {
    val result = lookup(segments, false);
    if (result != null) {
      return result.value;
    }
    return null;
  }

  public List<FileSystem> in(String key) {
    return in(keyPattern.split(key));
  }

  public List<FileSystem> in(String[] key) {
    val result = lookup(key, false);
    if (result != null) {
      val children = result.children;
      val r = new ArrayList<FileSystem>();
      for (val child : children) {
        val v = child.value;
        if (v != null) {
          r.add(v);
        }
      }
      return r;
    }
    return Collections.emptyList();
  }

  public List<FileSystem> list() {
    return StreamSupport.stream(spliterator(), false).collect(Collectors.toList());
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private Entry lookup(String[] segments, boolean create) {
    Entry current = root;
    for (val segment : segments) {
      val entries = current.children;

      Entry found = null;
      for (val child : entries) {
        if (segment.equals(child.name)) {
          found = child;
          break;
        }
      }

      if (found == null && create) {
        found = new Entry(segment);
        entries.add(found);
        current = found;
      } else if (found != null) {
        current = found;
      } else {
        return null; // not found
      }
    }
    return current;
  }

  @Override
  public Iterator<FileSystem> iterator() {
    return new RegistryIterator(root);
  }

  private static final class RegistryIterator implements Iterator<FileSystem> {
    private final Stack<Entry> stack;

    private RegistryIterator(@NonNull Entry current) {
      stack = new Stack<>();
      stack.push(current);
    }

    @Override
    public boolean hasNext() {
      return !stack.isEmpty();
    }

    @Override
    public FileSystem next() {
      while (!stack.empty()) {
        var nextElement = stack.pop();
        val children = nextElement.children;
        stack.addAll(children);
        if (nextElement.value != null) {
          return nextElement.value;
        }
      }
      throw new NoSuchElementException("Not here");
    }
  }

  static final class Entry {
    final String name;
    FileSystem value;
    List<Entry> children;

    private Entry(String name) {
      this(name, new ArrayList<>(0));
    }

    private Entry(String name, List<Entry> children) {
      this.name = name;
      this.children = children;
    }
  }
}
