package io.zephyr.kernel.module;

import io.zephyr.kernel.CoordinateSpecification;
import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.Dependency.ServicesResolutionStrategy;
import io.zephyr.kernel.Dependency.Type;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.core.ModuleDescriptor;
import io.zephyr.kernel.core.ModuleScanner;
import io.zephyr.kernel.core.PathSpecification;
import io.zephyr.kernel.core.PathSpecification.Mode;
import io.zephyr.kernel.core.SemanticVersion;
import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
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
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import lombok.AllArgsConstructor;
import lombok.val;

/**
 * ManifestModuleScanner
 *
 * <p>this class parses manifest of the form: {@code name: <name>} {@code dependencies:
 * <dependencySpec>} {@code dependencySpec: dependency+} {@code dependency:
 * type@<coordinate>\<<exportSpec>?<servicesSpec>?optional?\>} {@code exportSpec:
 * export:<none|import|export>} {@code servicesSpec: services:<true|false>} {@code optional:
 * optional}
 *
 * <p>
 *
 * <p>For instance: {@code service@io.sunshower:zephyr:version<export:none; services:true;
 * optional>
 * }
 */
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.DataflowAnomalyAnalysis"})
public final class ManifestModuleScanner implements ModuleScanner {

  /**
   *
   */
  static final String[] moduleDependencyModifiers = {
      "order", "optional", "re-export", "services", "exports-paths", "imports-paths"
  };

  @Override
  public Optional<ModuleDescriptor> scan(File file, URL source) {
    if (file == null || !file.exists()) {
      return Optional.empty();
    }

    try {
      try (val packageFile = new JarFile(file, true)) {
        return Optional.of(read(packageFile.getManifest(), file, source));
      }
    } catch (Throwable e) {
      return Optional.empty();
    }
  }

  private ModuleDescriptor read(Manifest manifest, File file, URL source) throws IOException {
    val attrs = manifest.getMainAttributes();
    val group = req(attrs, ModuleDescriptor.Attributes.GROUP);
    val name = req(attrs, ModuleDescriptor.Attributes.NAME);
    val version = req(attrs, ModuleDescriptor.Attributes.VERSION);
    val order = opt(attrs, ModuleDescriptor.Attributes.ORDER, 0);

    val type = Module.Type.parse(req(attrs, ModuleDescriptor.Attributes.TYPE));
    val description = attrs.getValue(ModuleDescriptor.Attributes.DESCRIPTION);
    val coordinate = new ModuleCoordinate(name, group, new SemanticVersion(version));
    val dependencies = parseDependencies(attrs);
    return new ModuleDescriptor(
        source, order, file, type, coordinate, dependencies, Collections.emptyList(), description);
  }

  private List<Dependency> parseDependencies(Attributes attrs) throws IOException {
    val deps = attrs.getValue(ModuleDescriptor.Attributes.DEPENDENCIES);
    if (!(deps == null || deps.trim().isEmpty())) {
      return readDependencies(new PushbackReader(new StringReader(deps)));
    }
    return Collections.emptyList();
  }

  private Integer opt(Attributes attrs, String name, Integer i) {
    val result = attrs.getValue(name);
    if (result == null || result.isBlank()) {
      return i;
    }
    return Integer.parseInt(name);
  }

  private String req(Attributes attrs, String key) {
    val v = attrs.getValue(key);
    if (v == null || v.isBlank()) {
      throw new IllegalArgumentException("Error: key '" + key + "' must not be null/empty");
    }
    return v;
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

  @SuppressWarnings("unchecked")
  void readDependency(List<Dependency> results, PushbackReader reader) throws IOException {
    val type = readDependencyType(reader);
    val coordinate = readModuleCoordinate(reader);
    if (peek(reader) == '<') {
      expectAndDiscard(reader, '<');
      Map<String, Object> values = readValues(reader);

      val dependency =
          new Dependency(
              (int) values.getOrDefault("order", 0),
              type,
              coordinate,
              (boolean) values.getOrDefault("optional", false),
              (boolean) values.getOrDefault("re-export", false),
              (ServicesResolutionStrategy)
                  values.getOrDefault("services", ServicesResolutionStrategy.None),
              (List<PathSpecification>)
                  values.getOrDefault("imports-paths", Collections.emptyList()),
              (List<PathSpecification>)
                  values.getOrDefault("exports-paths", Collections.emptyList()));

      results.add(dependency);
      readWhitespace(reader);
      expectAndDiscard(reader, '>');

    } else {
      val dependency = new Dependency(type, coordinate);
      results.add(dependency);
    }
  }

  private Map<String, Object> readValues(PushbackReader reader) throws IOException {
    Map<String, Object> values = new HashMap<>();
    while (peek(reader) != '>') {
      readWhitespace(reader);
      val moduleDependencyModifier = expectOneOf(reader, moduleDependencyModifiers);
      switch (moduleDependencyModifier) {
        case "optional":
          values.put("optional", true);
          break;
        case "services":
          readWhitespace(reader);
          expectAndDiscard(reader, '=');
          readWhitespace(reader);
          val result =
              ServicesResolutionStrategy.parse(expectOneOf(reader, "none", "import", "export"));
          readWhitespace(reader);
          values.put("services", result);
          break;
        case "re-export":
          readWhitespace(reader);
          values.put("re-export", true);
          readWhitespace(reader);
          break;
        case "order":
          readWhitespace(reader);
          expectAndDiscard(reader, '=');
          readWhitespace(reader);
          val order = readNumber(reader);
          values.put("order", order);
          break;
        case "imports-paths":
        case "exports-paths":
          readWhitespace(reader);
          expectAndDiscard(reader, '=');
          readWhitespace(reader);
          expectAndDiscard(reader, '[');
          val pathImports = readPathList(reader);
          expectAndDiscard(reader, ']');
          values.put(moduleDependencyModifier, pathImports);
          break;
      }
      readWhitespace(reader);
      if (peek(reader) == ';') {
        expectAndDiscard(reader, ';');
        readWhitespace(reader);
      }
    }
    return values;
  }

  private Integer readNumber(PushbackReader reader) throws IOException {
    val result = new StringBuilder();
    for (; ; ) {
      int ch = reader.read();
      if (!Character.isDigit(ch)) {
        reader.unread(ch);
        return Integer.parseInt(result.toString());
      }
      result.append((char) ch);
    }
  }

  private List<PathSpecification> readPathList(PushbackReader reader) throws IOException {

    val results = new ArrayList<PathSpecification>();
    while (peek(reader) != ']') {
      readWhitespace(reader);
      val mode = Mode.parse(expectOneOf(reader, "all", "just", "class"));
      readWhitespace(reader);
      expectAndDiscard(reader, ':');
      readWhitespace(reader);
      results.add(readPathSpec(mode, reader));
      if (peek(reader) == ',') {
        expectAndDiscard(reader, ',');
      }
    }
    return results;
  }

  private PathSpecification readPathSpec(Mode mode, PushbackReader reader) throws IOException {
    readWhitespace(reader);
    val pathSpec =
        readUntil(reader, "Expected one of (SPACE( ), TAB(\t), ',')", ' ', ',', '\t', ']');
    readWhitespace(reader);
    return new PathSpecification(mode, pathSpec.value);
  }

  CoordinateSpecification readModuleCoordinate(PushbackReader reader) throws IOException {
    expectAndDiscard(reader, '@');
    val group = readUntil(reader, "(missing group)", ':');
    expectAndDiscard(reader, ':');
    val artifact = readUntil(reader, "(missing artifact)", ':');
    expectAndDiscard(reader, ':');
    MatchResult version;
    if (nextIsOneOf(reader, '(', '[', ']', ')')) {
      val ch = reader.read();
      version = new MatchResult(((char) ch) + readRangeSpec(reader).value + ((char) reader.read()),
          ch);
      readUntil(reader, "expected deliminter or whitespace", true, ',', '<', '\r', '\n', '\t', ' ');
    } else {
      version = readUntil(reader, "(missing version)", true, ',', '<', '\r', '\n', '\t', ' ');
    }
    checkForNewLine(reader);

    return new CoordinateSpecification(group.value, artifact.value, version.value);
  }

  private MatchResult readRangeSpec(PushbackReader reader) throws IOException {
    return readUntil(reader, "(expected range close: {']', ')', '(', '[', '[', '('}", true, '[', '(', ']',
        ')');
  }

  private boolean nextIsOneOf(PushbackReader reader, char... chars) throws IOException {
    val ch = peek(reader);
    for (val c : chars) {
      if (c == ch) {
        return true;
      }
    }
    return false;
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
    return readUntil(reader, cause, true, chs);
  }

  private MatchResult readUntil(PushbackReader reader, String cause, boolean oreof, char... chs)
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
    if (oreof) {
      return new MatchResult(result.toString(), a);
    }
    throw new NoSuchElementException(
        String.format(
            "Error: expected '%s', got '%s' EOF.  Reason: ", Arrays.toString(chs), result, cause));
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
            String.format(
                "Expected one of: %s.  Reached EOF at %s instead",
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
        String.format("Expected one of: %s.  Got '%s' instead", Arrays.toString(values), buffer));
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
