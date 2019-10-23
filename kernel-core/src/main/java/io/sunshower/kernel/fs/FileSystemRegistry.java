package io.sunshower.kernel.fs;

import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystem;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FileSystemRegistry implements Iterable<FileSystem> {

  private static final Pattern keyPattern = Pattern.compile("\\.");
  final Entry root = new Entry(null);

  private int size;

  public FileSystem add(@NonNull String key, @NonNull FileSystem fileSystem) {
    val current = lookup(key, true);
    val existing = current.value;
    current.value = fileSystem;
    size++;
    return existing;
  }

  public int size() {
    return size;
  }

  public FileSystem remove(@NonNull String key) {
    val segments = keyPattern.split(key);
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
    size--;
    return result;
  }

  public FileSystem get(@NonNull String key) {
    val result = lookup(key, false);
    if (result != null) {
      return result.value;
    }
    return null;
  }

  public List<FileSystem> list() {
    return StreamSupport.stream(spliterator(), false).collect(Collectors.toList());
  }

  private Entry lookup(String key, boolean create) {
    val segments = keyPattern.split(key);
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
      }
    }
    return current;
  }

  @NotNull
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
        if (nextElement.value != null) {
          stack.addAll(children);
          return nextElement.value;
        } else {
          for (int i = 0; i < children.size(); i++) {
            val ch = children.get(i);
            if (ch.value != null) {
              stack.addAll(children.subList(i + 1, children.size()));
              return ch.value;
            } else {
              stack.push(ch);
            }
          }
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
