package io.sunshower.kernel.launch;

import io.sunshower.common.i18n.Localization;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Getter
@Setter
@Slf4j
@CommandLine.Command(resourceBundle = "i18n.io.sunshower.kernel.launch.KernelOptions")
public class KernelOptions {


    private ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public interface SystemProperties {
        String UserHome      = "user.home";
        String SunshowerHome = "sunshower.home";
    }

    public interface EnvironmentVariables {
        String SunshowerHome = "SUNSHOWER_HOME";
    }

    @CommandLine.Option(names = {
            "-s", "--storage"
    }, descriptionKey = "storage.description")
    String storage;

    @CommandLine.Option(names = {
            "-h", "--help"
    }, descriptionKey = "storage.help")
    boolean help;


    private Localization localization;


    public KernelOptions satisfy(String... args) {
        val cmd    = new CommandLine(this);
        val bundle = cmd.getResourceBundle();
        localization = new Localization(bundle);
        cmd.parseArgs(args);
        satisfyStorage(bundle);
        return this;
    }

    public Localization getLocalization() {
        if (localization == null) {
            localization = new Localization(new CommandLine(this).getResourceBundle());
        }
        return localization;
    }

    private void satisfyStorage(ResourceBundle resourceBundle) {
        val locations = Arrays.asList(
                new Location(
                        SystemProperties.SunshowerHome,
                        "location.property"
                ),
                new Location(
                        EnvironmentVariables.SunshowerHome,
                        "location.environment"
                ),
                new Location(
                        SystemProperties.UserHome,
                        "location.property"
                )
        );

        for (val location : locations) {
            if (check(resourceBundle, location.location, location.locationKey)) {
                return;
            }
        }
    }

    boolean check(ResourceBundle resourceBundle, String location, String locationKey) {
        var loc = resourceBundle.getString(locationKey);
        log.info(format(resourceBundle, "sunshower.home.search", loc, location));
        var result = System.getProperty(location);
        if (result != null) {
            log.info(format(resourceBundle, "sunshower.home.found", loc, location, result));
            storage = result.trim();
            val path = Paths.get(storage);
            if (ensureExists(resourceBundle, path)) {
                val dir = path.resolve(".sunshower");
                if (ensureExists(resourceBundle, dir)) {
                    return ensureExists(resourceBundle, dir.resolve("plugins"));
                }
            }
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

    @AllArgsConstructor
    static class Location {
        final String location;
        final String locationKey;
    }
}
