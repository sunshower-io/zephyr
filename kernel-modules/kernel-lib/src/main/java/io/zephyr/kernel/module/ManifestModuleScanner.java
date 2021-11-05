package io.zephyr.kernel.module;

import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.Dependency.ServicesResolutionStrategy;
import io.zephyr.kernel.Dependency.Type;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.core.ModuleDescriptor;
import io.zephyr.kernel.core.ModuleScanner;
import io.zephyr.kernel.core.SemanticVersion;
import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.val;

/**
 * ManifestModuleScanner
 * <p>
 * this class parses manifest of the form: {@code name: <name>} {@code dependencies:
 * <dependencySpec>} {@code dependencySpec: dependency+} {@code dependency:
 * type@<coordinate>\<<exportSpec>?<servicesSpec>?optional?\>} {@code exportSpec:
 * export:<none|import|export>} {@code servicesSpec: services:<true|false>} {@code optional:
 * optional}
 * <p>
 * <p>
 * For instance:
 *
 * <code>
 * service@io.sunshower:zephyr:version<export:none; services:true; optional>
 *
 * </code>
 */
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.DataflowAnomalyAnalysis"})
public final class ManifestModuleScanner implements ModuleScanner {

  /**
   *
   */
  static final String[] moduleDependencyModifiers = {
      "optional",
      "export",
      "services",
      "includes",
      "excludes"
  };

  @Override
  public Optional<ModuleDescriptor> scan(File file, URL source) {
    return Optional.empty();
  }

  List<Dependency> readDependencies(PushbackReader reader) throws IOException {
    val results = new ArrayList<Dependency>();

    for (; ; ) {
      if (eof(reader)) {
        return results;
      }
      readWhitespace(reader);
      readDependency(results, reader);
      readWhitespace(reader);

      if (eof(reader)) {
        return results;
      }
      if (peek(reader) == ',') {
        expectAndDiscard(reader, ',');
      }
      val nl = peek(reader);
      if (nl == '\n' || nl == '\uFFFF' || nl == '\r') {
        return results;
      }

    }
  }

  private boolean eof(PushbackReader reader) throws IOException {
    int ch = reader.read();
    if (ch == -1) {
      return true;
    }
    reader.unread(ch);
    return false;
  }

  private void readWhitespace(PushbackReader reader) throws IOException {
    for (; ; ) {
      int ch = reader.read();
      if (ch == -1) {
        return;
      }
      if (!Character.isWhitespace(ch)) {
        reader.unread(ch);
        return;
      }
    }

  }

  void readDependency(List<Dependency> results, PushbackReader reader) throws IOException {
    val type = readDependencyType(reader);
    val coordinate = readModuleCoordinate(reader);
    if (peek(reader) == '<') {
      expectAndDiscard(reader, '<');
      readWhitespace(reader);
      val moduleDependencyModifier = expectOneOf(reader, moduleDependencyModifiers);
      Map<String, Object> values = new HashMap<>();
      while (peek(reader) != '>') {
        switch (moduleDependencyModifier) {
          case "optional": {
            values.put("optional", true);
          }
          case

        }
      }

      val dependency = new Dependency(type, coordinate,
          (boolean) values.getOrDefault("optional", false), false,
          ServicesResolutionStrategy.None, Collections.emptyList(), Collections.emptyList());

      results.add(dependency);

      readWhitespace(reader);
      expectAndDiscard(reader, '>');


    } else {
      val dependency = new Dependency(type, coordinate);
      results.add(dependency);
    }

  }

  ModuleCoordinate readModuleCoordinate(PushbackReader reader) throws IOException {
    expectAndDiscard(reader, '@');
    val group = readUntil(reader, "(missing group)", ':');
    expectAndDiscard(reader, ':');
    val artifact = readUntil(reader, "(missing artifact)", ':');
    expectAndDiscard(reader, ':');
    val version = readUntil(reader, "(missing version)", ',', '<', '\r', '\n', '\t', ' ');
    checkForNewLine(reader);

    return new ModuleCoordinate(artifact.value, group.value,
        new SemanticVersion(version.value));
  }

  private void checkForNewLine(PushbackReader reader) throws IOException {
    if (peek(reader) == '\r') {
      expectAndDiscard(reader, '\r');
      if (peek(reader) == '\n') {
        expectAndDiscard(reader, '\n');
      }
    }
  }

  private char peek(PushbackReader reader) throws IOException {
    int a = reader.read();
    reader.unread(a);
    return (char) a;
  }

  private void expectAndDiscard(PushbackReader reader, char c) throws IOException {
    int ch = reader.read();
    if (((char) ch) != c) {
      throw new IllegalArgumentException(String.format("Expected '%c', got '%c'", c, (char) ch));
    }
  }

  private MatchResult readUntil(PushbackReader reader, String cause, char... chs)
      throws IOException {
    val result = new StringBuilder();
    int a;
    while ((a = reader.read()) != -1) {
      char ch = (char) a;
      for (char s : chs) {
        if (ch == s) {
          reader.unread(a);
          return new MatchResult(result.toString(), a);
        }
      }
      result.append(ch);
    }
    throw new NoSuchElementException(
        String.format("Error: expected '%s', got '%s' EOF.  Reason: ", Arrays.toString(chs), result,
            cause));
  }

  Type readDependencyType(PushbackReader reader) throws IOException {
    val result = expectOneOf(reader, "library", "service");
    return Type.parse(result);
  }

  String expectOneOf(PushbackReader reader, String... values) throws IOException {
    val excluded = new BitSet();
    val maxTokenLength = maxSize(values);
    val buffer = new StringBuilder(maxTokenLength);
    for (var i = 0; i < maxTokenLength; i++) {
      int ch = reader.read();
      if (ch == -1) {
        throw new IllegalArgumentException(
            String.format("Expected one of: %s.  Reached EOF at %s instead",
                Arrays.toString(values), buffer));
      }
      buffer.append((char) ch);
      for (int j = 0; j < values.length; j++) {
        if (excluded.get(j)) {
          continue;
        }
        val value = values[j];
        if (i < value.length()) {
          if (((char) ch) != value.charAt(i)) {
            excluded.set(j);
          } else {
            for (int k = 0; k < values.length; k++) {
              if (!excluded.get(k) && value.length() - 1 == i) {
                return values[k];
              }
            }
          }
        } else {
          excluded.set(j);
        }
      }
    }

    for (int j = 0; j < values.length; j++) {
      if (!excluded.get(j)) {
        return values[j];
      }
    }

    throw new IllegalArgumentException(
        String.format("Expected one of: %s.  Got '%s' instead",
            Arrays.toString(values), buffer));
  }

  private int maxSize(String[] values) {
    var current = 0;
    for (val v : values) {
      val len = v.length();
      if (len > current) {
        current = len;
      }
    }
    return current;
  }

  @AllArgsConstructor
  private static class MatchResult {

    final String value;
    final int character;
  }


}
