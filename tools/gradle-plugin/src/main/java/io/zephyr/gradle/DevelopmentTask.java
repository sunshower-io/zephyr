package io.zephyr.gradle;

import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;

public class DevelopmentTask extends DefaultTask {

  @TaskAction
  public void perform() throws IOException, InterruptedException {
    val project = getProject();
    val zephyr =
        Zephyr.builder().homeDirectory(randomDir(project)).create(getClass().getClassLoader());

    Runtime.getRuntime().addShutdownHook(new Thread(() -> shutDown(zephyr)));
    installKernelModules(zephyr, project);
    val urls = collectUrls(project, "zephyrModule");

    collectSelf(project, urls);
    zephyr.install(urls);
    start(zephyr, urls);
    DevelopmentPlugin.setInstance(zephyr);

    synchronized (this) {
      wait();
    }
  }

  private void shutDown(Zephyr zephyr) {
    getLogger().log(LogLevel.INFO, "Shutting down plugin...");
    val plugins = zephyr.getPlugins(Lifecycle.State.Active);
    zephyr.stop(
        plugins.stream()
            .map(t -> t.getCoordinate().toCanonicalForm())
            .collect(Collectors.toList()));
    getLogger().log(LogLevel.INFO, "Successfully shut down plugins");
  }

  private void installKernelModules(Zephyr zephyr, Project project) throws MalformedURLException {
    val urls = collectUrls(project, "zephyrKernelModule");
    zephyr.startup();
    zephyr.install(urls);
    zephyr.restart();
  }

  private void start(Zephyr zephyr, Set<URL> urls) {
    zephyr.start(
        zephyr.getPlugins().stream()
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

  private void collectSelf(Project project, Set<URL> urls) {
    val plugins = project.getPlugins();

    //    plugins.withType(
    //        JavaPlugin.class,
    //        javaPlugin -> {
    //          project.getTasks().withType(Jar.class).configureEach(jar -> add(jar, urls));
    //        });
    //
    plugins.withType(
        WarPlugin.class,
        javaPlugin -> {
          project.getTasks().withType(War.class).configureEach(jar -> add(jar, urls));
        });
  }

  private void add(Jar jar, Set<URL> urls) {

    val archive = jar.getArchiveFile();
    if (archive.isPresent()) {
      val file = archive.get().getAsFile();
      try {
        val url = file.toURI().toURL();
        getLogger().info("Successfully resolved plugin {} for deployment", file.getAbsolutePath());
        urls.add(url);
      } catch (MalformedURLException ex) {
        getLogger()
            .log(
                LogLevel.ERROR,
                "Failed to resolve file {}.  Reason: {}",
                file.getAbsolutePath(),
                ex.getMessage());
      }
    }
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

  private File randomDir(Project project) throws IOException {
    val buildDir = project.getBuildDir();
    val zephyrDir = new File(buildDir, "zephyr_tmp");
    val instanceList = new InstanceList(zephyrDir);
    DevelopmentPlugin.setInstanceList(instanceList);
    val dir = UUID.randomUUID().toString();
    val result = new File(zephyrDir, dir);
    instanceList.save(new InstanceList.Instance(dir));

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
