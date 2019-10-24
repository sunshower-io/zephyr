package io.sunshower.kernel.module;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.ModuleCoordinate;
import io.sunshower.kernel.core.ModuleDescriptor;
import io.sunshower.kernel.core.ModuleScanner;
import io.sunshower.kernel.core.SemanticVersion;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import lombok.val;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ManifestModuleScanner implements ModuleScanner {
  static final Pattern commaSeparated = Pattern.compile(",");
  static final Pattern coordinateSeparator = Pattern.compile(":");
  private static final int SEGMENT_SIZE = 3;

  @Override
  public Optional<ModuleDescriptor> scan(File file, URL source) {
    if (!file.exists()) {
      return Optional.empty();
    }

    try {
      try (val packageFile = new JarFile(file, true)) {
        return Optional.of(read(packageFile.getManifest(), file, source));
      }
    } catch (IOException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private ModuleDescriptor read(Manifest manifest, File file, URL source) {

    val attrs = manifest.getMainAttributes();
    val group = req(attrs, ModuleDescriptor.Attributes.GROUP);
    val name = req(attrs, ModuleDescriptor.Attributes.NAME);
    val version = req(attrs, ModuleDescriptor.Attributes.VERSION);
    val order = opt(attrs, ModuleDescriptor.Attributes.ORDER, 0);

    val type = Module.Type.parse(req(attrs, ModuleDescriptor.Attributes.TYPE));
    val description = attrs.getValue(ModuleDescriptor.Attributes.DESCRIPTION);
    val coordinate = new ModuleCoordinate(name, group, new SemanticVersion(version));
    val dependencies = parseDependencies(attrs);
    return new ModuleDescriptor(type, source, order, file, coordinate, dependencies, description);
  }

  private Integer opt(Attributes attrs, String name, Integer i) {
    val result = attrs.getValue(name);
    if (result == null || result.isBlank()) {
      return i;
    }
    return Integer.parseInt(name);
  }

  private List<Coordinate> parseDependencies(Attributes attrs) {
    val deps = attrs.getValue(ModuleDescriptor.Attributes.DEPENDENCIES);
    if (!(deps == null || deps.isBlank())) {
      return parseDependencies(deps);
    }
    return Collections.emptyList();
  }

  private List<Coordinate> parseDependencies(String deps) {
    val dependencyList = commaSeparated.split(deps);
    val results = new ArrayList<Coordinate>(dependencyList.length);
    for (val dependencyString : dependencyList) {
      if (!dependencyString.isBlank()) {
        parseCoordinate(results, dependencyString);
      }
    }
    return results;
  }

  private void parseCoordinate(List<Coordinate> results, String dependencyString) {
    val segments = coordinateSeparator.split(dependencyString);
    if (segments.length != SEGMENT_SIZE) {
      throw new IllegalArgumentException(
          "Error: invalid dependency string: '"
              + dependencyString
              + "'.  Must be of format <group>:<artifact>:<versionspec>");
    }
    val result = new ModuleCoordinate(segments[1], segments[0], new SemanticVersion(segments[2]));
    results.add(result);
  }

  private String req(Attributes attrs, String key) {
    val v = attrs.getValue(key);
    if (v == null || v.isBlank()) {
      throw new IllegalArgumentException("Error: key '" + key + "' must not be null/empty");
    }
    return v;
  }
}
