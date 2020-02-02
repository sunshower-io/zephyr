package io.zephyr.gradle;

import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

public class DevelopmentTask extends DefaultTask {

  @TaskAction
  public void perform() throws MalformedURLException {
    val project = getProject();
    val zephyr = Zephyr.builder().homeDirectory(randomDir(project)).create();
    val urls = collectUrls(project);
    zephyr.install(urls);
    start(zephyr, urls);
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

  private Set<URL> collectUrls(Project project) throws MalformedURLException {
    val urls = new HashSet<URL>();

    for (val cfg : project.getConfigurations()) {
      for (val artifact : cfg.getResolvedConfiguration().getResolvedArtifacts()) {
        urls.add(artifact.getFile().toURI().toURL());
      }
    }
    return urls;
  }

  private File randomDir(Project project) {
    val buildDir = project.getBuildDir();
    val zephyrDir = new File(buildDir, "zephyr_tmp");
    return new File(zephyrDir, UUID.randomUUID().toString());
  }
}
