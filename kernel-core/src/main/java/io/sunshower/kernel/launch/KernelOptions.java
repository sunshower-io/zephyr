package io.sunshower.kernel.launch;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;

import io.sunshower.kernel.common.i18n.Localization;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Getter
@Setter
@Slf4j
@CommandLine.Command(resourceBundle = "i18n.io.sunshower.kernel.launch.KernelOptions")
public class KernelOptions {

  private ExecutorService executorService = Executors.newScheduledThreadPool(4);

  public interface SystemProperties {
    String UserHome = "user.home";
    String SunshowerHome = "sunshower.home";
  }

  public interface EnvironmentVariables {
    String SunshowerHome = "SUNSHOWER_HOME";
  }

  @CommandLine.Option(
    names = {"-s", "--storage"},
    descriptionKey = "storage.description"
  )
  String storage;

  @CommandLine.Option(
    names = {"-h", "--help"},
    descriptionKey = "storage.help"
  )
  boolean help;

  /** Current localization */
  private Localization localization;

  /** Storage for plugin files */
  private Path pluginDirectory;

  /** Storage for plugin temp workspace, plugin directories */
  private Path workspaceDirectory;

  /** Plugin temp directory--plugin files are copied here before processing */
  private Path pluginDataDirectory;

  /** */
  private Path kernelModuleDirectory;

  private Path kernelModuleDataDirectory;

  public KernelOptions satisfy(String... args) {
    val cmd = new CommandLine(this);
    val bundle = cmd.getResourceBundle();
    localization = new Localization(bundle);
    cmd.parseArgs(args);
    satisfyStorage(bundle);
    return this;
  }

  public final boolean overrideStorage(String result) {
    return overrideStorage(getLocalization().getBundle(), result);
  }

  final boolean overrideStorage(ResourceBundle resourceBundle, String result) {
    storage = result.trim();
    val path = Paths.get(storage);
    if (ensureExists(resourceBundle, path)) {
      val dir = path.resolve(".sunshower");
      if (ensureExists(resourceBundle, dir)) {

        pluginDirectory = dir.resolve("plugins");
        workspaceDirectory = dir.resolve("workspace");
        kernelModuleDirectory = dir.resolve("kernel-modules");
        pluginDataDirectory = workspaceDirectory.resolve("plugins");
        kernelModuleDataDirectory = workspaceDirectory.resolve("modules");
        return ensureExists(resourceBundle, pluginDirectory)
            && ensureExists(resourceBundle, workspaceDirectory)
            && ensureExists(resourceBundle, pluginDataDirectory)
            && ensureExists(resourceBundle, kernelModuleDirectory)
            && ensureExists(resourceBundle, kernelModuleDataDirectory);
      }
    }
    return false;
  }

  public Localization getLocalization() {
    if (localization == null) {
      localization = new Localization(new CommandLine(this).getResourceBundle());
    }
    return localization;
  }

  private void satisfyStorage(ResourceBundle resourceBundle) {
    val locations =
        Arrays.asList(
            new Location(SystemProperties.SunshowerHome, "location.property", System::getProperty),
            new Location(
                EnvironmentVariables.SunshowerHome, "location.environment", System::getenv),
            new Location(SystemProperties.UserHome, "location.property", System::getProperty));

    for (val location : locations) {
      if (check(resourceBundle, location)) {
        return;
      }
    }
  }

  boolean check(ResourceBundle resourceBundle, Location location) {
    var loc = resourceBundle.getString(location.locationKey);
    log.info(format(resourceBundle, "sunshower.home.search", loc, location));
    var result = location.resolver.apply(location.location);
    if (result != null) {
      log.info(format(resourceBundle, "sunshower.home.found", loc, location, result));
      return overrideStorage(resourceBundle, result.trim());
    }
    return false;
  }

  private boolean ensureExists(ResourceBundle resourceBundle, Path path) {
    log.info(format(resourceBundle, "check.file", path));
    val file = path.toFile();

    if (file.exists() && file.isFile()) {
      log.error(format(resourceBundle, "file.error.isFile", file.getAbsolutePath()));
      return false;
    }

    if (file.exists() && !file.canWrite()) {
      log.error(format(resourceBundle, "file.error.cantWrite", file.getAbsolutePath()));
      return false;
    }

    if (file.exists() && !file.canRead()) {
      log.error(format(resourceBundle, "file.error.cantRead", file.getAbsolutePath()));
      return false;
    }

    if (!file.exists()) {
      log.info(format(resourceBundle, "file.create.attempting", file.getAbsolutePath()));
      if (!file.mkdirs()) {
        log.error(format(resourceBundle, "file.error.cantCreate"));
        return false;
      }
      log.info(format(resourceBundle, "file.created", file.getAbsolutePath()));
    }
    log.info(format(resourceBundle, "file.exists", file.getAbsolutePath()));
    return true;
  }

  String format(ResourceBundle bundle, String key, Object... args) {
    return MessageFormat.format(bundle.getString(key), (Object[]) args);
  }

  static class Location {
    final String location;
    final String locationKey;
    private final Function<String, String> resolver;

    public Location(String location, String key, Function<String, String> resolver) {
      this.location = location;
      this.locationKey = key;
      this.resolver = resolver;
    }
  }
}
