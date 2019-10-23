package io.sunshower.common.io;

import java.io.File;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import lombok.NonNull;
import lombok.val;

public class Files {

  public static final String separator = File.separatorChar == '\\' ? "\\\\" : File.separator;

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public static Path toPath(@NonNull String... segments) {
    Path path = Path.of(segments[0]);
    for (int i = 1; i < segments.length; i++) {
      path = path.resolve(segments[i]);
    }
    return path;
  }

  public static File check(File file, FilePermissionChecker.Type... checks)
      throws AccessDeniedException {
    for (val check : checks) {
      if (!check.check(file)) {
        throw new AccessDeniedException(file.getAbsolutePath(), null, check.name());
      }
    }
    return file;
  }
}
