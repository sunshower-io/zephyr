package io.zephyr.bundle.linux;

import static java.lang.String.format;

import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExecutingBundler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import lombok.val;

public class WarpSelfExecutingLinuxBundler implements SelfExecutingBundler {
  @Override
  public void load(File workspaceDirectory, Log log) {

    val file = new File(workspaceDirectory, "warp").getAbsoluteFile();
    if (!exists(workspaceDirectory)) {
      val currentFile = getCodeLocation();
      try (val inputStream = currentFile.openStream()) {
        log.info(format("Copying file '%s' to '%s'", "linux/exe/warp", file));
        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info(format("Successfully copied file '%s' to '%s'", "linux/exe/warp", file));
        log.info(format("Making file '%s' executable...", file));
        setExecutable(file.toPath());
        log.info(format("Successfully set '%s' to be executable", file));
      } catch (IOException ex) {
        throw new IllegalArgumentException("Failed to extract bundler: ", ex);
      }
    } else {
      log.info(format("File '%s' already exists--not re-extracting", file.getAbsolutePath()));
    }
  }

  private void setExecutable(Path path) throws IOException {
    val permissions = new HashSet<PosixFilePermission>();
    permissions.add(PosixFilePermission.OWNER_EXECUTE);
    permissions.add(PosixFilePermission.OWNER_WRITE);
    Files.setPosixFilePermissions(path, permissions);
  }

  @Override
  public void create(BundleOptions options, Log log) {}

  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return platform == BundleOptions.Platform.Linux
        && architecture == BundleOptions.Architecture.X64;
  }

  private URL getCodeLocation() {
    return WarpSelfExecutingLinuxBundler.class.getClassLoader().getResource("exe/linux/warp");
  }

  private boolean exists(File workspaceDirectory) {
    return Files.exists(Paths.get(workspaceDirectory.getAbsolutePath(), "warp"));
  }
}
