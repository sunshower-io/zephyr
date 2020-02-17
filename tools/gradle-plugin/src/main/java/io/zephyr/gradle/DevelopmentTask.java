package io.zephyr.gradle;

import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

public class DevelopmentTask extends DefaultTask {

  @TaskAction
  public void perform() throws MalformedURLException, FileNotFoundException {
    val project = getProject();
    val zephyr =
        Zephyr.builder().homeDirectory(randomDir(project)).create(getClass().getClassLoader());

    installKernelModules(zephyr, project);
    val urls = collectUrls(project, "zephyrModule");
    zephyr.install(urls);
    start(zephyr, urls);
  }

  private void installKernelModules(Zephyr zephyr, Project project) throws MalformedURLException {
    val urls = collectUrls(project, "zephyrKernelModule");
    zephyr.startup();
    zephyr.install(urls);
    zephyr.restart();
  }

  private void start(Zephyr zephyr, Set<URL> urls) {
    zephyr.start(
        zephyr
            .getPlugins()
            .stream()
            .filter(t -> isSource(t, urls))
            .map(Module::getCoordinate)
            .map(Coordinate::toCanonicalForm)
            .collect(Collectors.toList()));
  }

  private boolean isSource(Module t, Set<URL> urls) {
    try {
      return urls.contains(t.getSource().getLocation().toURL());
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Set<URL> collectUrls(Project project, String configuration) throws MalformedURLException {
    val urls = new HashSet<URL>();

    for (val cfg : project.getConfigurations()) {
      if (cfg.getName().equals(configuration)) {
        for (val artifact : cfg.getResolvedConfiguration().getResolvedArtifacts()) {
          urls.add(artifact.getFile().toURI().toURL());
        }
      }
    }
    logUrls(urls);
    return urls;
  }

  private void logUrls(HashSet<URL> urls) {
    val logger = getLogger();
    if (logger.isEnabled(LogLevel.DEBUG)) {
      logger.log(LogLevel.DEBUG, "Installing plugins at URLs: ");
      for (val url : urls) {
        logger.log(LogLevel.DEBUG, "\t{}", url);
      }
    }
  }

  private File randomDir(Project project) throws FileNotFoundException {
    val buildDir = project.getBuildDir();
    val zephyrDir = new File(buildDir, "zephyr_tmp");
    val result = new File(zephyrDir, UUID.randomUUID().toString());

    val logger = getLogger();
    createIfNecessary(result, logger);
    if (logger.isEnabled(LogLevel.DEBUG)) {
      logger.debug("Zephyr installed at {}", result);
    }
    return result;
  }

  private void createIfNecessary(File result, Logger logger) throws FileNotFoundException {
    if (result.exists() && result.isDirectory()) {
      logger.info("Directory {} exists...continuing", result);
    }

    if (!result.exists()) {
      logger.info("Directory {} does not exist--attempting to create", result);
      if (!result.mkdirs()) {
        throw new FileNotFoundException(
            "Failed to start Zephyr.  Could not create " + result.getAbsolutePath());
      }
      logger.info("Successfully created directory {}", result.getAbsolutePath());
    }
  }
}
