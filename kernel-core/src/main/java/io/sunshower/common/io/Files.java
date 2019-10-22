package io.sunshower.common.io;

import lombok.val;

import java.io.File;
import java.nio.file.AccessDeniedException;

public class Files {

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
