package io.sunshower.common.io;

import java.io.File;
import java.nio.file.AccessDeniedException;
import lombok.val;

public class Files {

  public static final String separator = File.separatorChar == '\\' ? "\\\\" : File.separator;

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
