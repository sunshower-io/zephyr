package io.sunshower.kernel.ext.scanner;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import io.zephyr.kernel.CoordinateSpecification;
import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.InvalidPluginDescriptorException;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.core.ModuleDescriptor;
import io.zephyr.kernel.core.ModuleScanner;
import io.zephyr.kernel.log.Logging;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

/**
 * this parses plugin descriptors of the format:
 *
 * <p>plugin: name: io.sunshower.yaml group: io.sunshower version: 1.0.0 description: Enables kernel
 * modules and plugins to implement their plugin configurations in YAML dependencies: - dependency:
 * required: true group: io.sunshower.whatever artifact: whatever version: 1.0.0
 */
@SuppressWarnings({"unchecked", "PMD.DataflowAnomalyAnalysis"})
public class YamlPluginDescriptorScanner implements ModuleScanner {

  /**
   * Respect conventions--the file should be found if it's placed in META-INF/plugin.yml whether
   * we're scanning a WAR or a JAR
   */
  static final Set<String> SEARCH_PATHS =
      Set.of(
          "META-INF/plugin.yml",
          "classes/META-INF/plugin.yml",
          "WEB-INF/classes/META-INF/plugin.yml",
          "BOOT-INF/classes/META-INF/plugin.yml");

  static final Logger log = Logging.get(YamlPluginDescriptorScanner.class);

  /** plugin constants */
  static final String ROOT = "plugin";

  static final String NAME = "name";
  static final String GROUP = "group";
  static final String VERSION = "version";
  static final String DESCRIPTION = "description";
  static final String DEPENDENCIES = "dependencies";
  static final String MODULE_TYPE = "type";

  /** dependency constants */
  static final String DEPENDENCY = "dependency";

  static final String REQUIRED = "required";
  static final String DEPENDENCY_TYPE = MODULE_TYPE;

  static final String DEPENDENCY_GROUP = GROUP;
  static final String DEPENDENCY_NAME = NAME;
  static final String DEPENDENCY_VERSION = VERSION;

  @Override
  public Optional<ModuleDescriptor> scan(File file, URL source) {
    if (log.isLoggable(Level.INFO)) {
      log.log(Level.INFO, "yaml.descriptor.scanner.starting", new Object[] {file, source});
    }

    for (val entryName : SEARCH_PATHS) {
      val explodedFile = new File(file, entryName);
      if (explodedFile.exists()) {
        try (val reader =
            new InputStreamReader(
                Files.newInputStream(explodedFile.toPath(), StandardOpenOption.READ))) {
          val result = getModuleDescriptor(explodedFile, source, reader);
          if (log.isLoggable(Level.FINE)) {
            log.log(
                Level.FINE,
                "yaml.descriptor.scanner.found",
                new Object[] {entryName, file, source});
          }
          return result;
        } catch (IOException e) {
          log.log(
              Level.INFO,
              "yaml.descriptor.scanner.error",
              new Object[] {file, source, e.getMessage()});
          log.log(Level.FINEST, "Full trace", e);
        }
      }

      try (val zipfile = new JarFile(file, true)) {
        val entry = zipfile.getJarEntry(entryName);

        if (entry != null) {
          val opt = doParse(file, zipfile, entry, source);
          if (opt.isPresent()) {
            return opt;
          }
        }
      } catch (IOException e) {
        log.log(
            Level.INFO,
            "yaml.descriptor.scanner.error",
            new Object[] {file, source, e.getMessage()});
        log.log(Level.FINEST, "Full trace", e);
      }
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  private Optional<ModuleDescriptor> doParse(
      File sourceFile, JarFile zipfile, JarEntry entry, URL source) throws IOException {
    try (val reader = new InputStreamReader(zipfile.getInputStream(entry))) {
      return getModuleDescriptor(sourceFile, source, reader);
    }
  }

  private Optional<ModuleDescriptor> getModuleDescriptor(
      File sourceFile, URL source, InputStreamReader reader) throws YamlException {
    val yamlInput = new YamlReader(reader);

    val descriptorDocument = (Map<String, Object>) yamlInput.read();
    val pluginDescriptor = (Map<String, Object>) descriptorDocument.get(ROOT);
    if (pluginDescriptor == null) {
      throw new InvalidPluginDescriptorException("expected root element 'plugin' in plugin.yml");
    }

    val group = require(pluginDescriptor, GROUP);
    val name = require(pluginDescriptor, NAME);
    val version = require(pluginDescriptor, VERSION);
    val description = optional(pluginDescriptor, DESCRIPTION);
    var moduleType = optional(pluginDescriptor, MODULE_TYPE);

    final Module.Type modType;
    if (moduleType != null) {
      modType = Module.Type.parse(moduleType);
    } else {
      modType = Module.Type.Plugin;
    }

    val dependencyNode = (List<Object>) pluginDescriptor.get(DEPENDENCIES);

    final List<Dependency> dependencies;

    if (dependencyNode != null) {
      dependencies = new ArrayList<>(dependencyNode.size());
      for (val dep : dependencyNode) {
        parseDependency(dependencies, (Map<String, Object>) dep);
      }
    } else {
      dependencies = Collections.emptyList();
    }

    val descriptor =
        new ModuleDescriptor(
            source,
            0,
            sourceFile,
            modType,
            ModuleCoordinate.create(group, name, version),
            dependencies,
            Collections.emptyList(),
            description);
    return Optional.of(descriptor);
  }

  @SuppressWarnings("unchecked")
  private void parseDependency(List<Dependency> dependencies, Map<String, Object> dep) {
    val dependency = (Map<String, Object>) dep.get(DEPENDENCY);

    val depType = optional(dependency, DEPENDENCY_TYPE);
    final Dependency.Type type;
    if (depType != null) {
      type = Dependency.Type.parse(depType);
    } else {
      type = Dependency.Type.Library;
    }

    /** todo: we need to fix some things with this */
    val required = Boolean.valueOf(optional(dependency, REQUIRED));

    val depName = require(dependency, DEPENDENCY_NAME);
    val depGroup = require(dependency, DEPENDENCY_GROUP);
    val depVersion = require(dependency, DEPENDENCY_VERSION);
    val dependencyDescriptor =
        new Dependency(type, new CoordinateSpecification(depGroup, depName, depVersion));
    dependencies.add(dependencyDescriptor);
  }

  private String optional(Map<String, Object> pluginDescriptor, String description) {
    val result = pluginDescriptor.get(description);
    return (String) result;
  }

  private String require(Map<String, Object> pluginDescriptor, String group) {
    val result = pluginDescriptor.get(group);
    if (result == null) {

      throw new InvalidPluginDescriptorException(
          "Expected element '" + group + "' to be defined--it wasn't");
    }
    if (!(result instanceof String)) {
      throw new InvalidPluginDescriptorException(
          "Expected element '" + group + "' to be of type String--it wasn't");
    }

    return (String) result;
  }
}
