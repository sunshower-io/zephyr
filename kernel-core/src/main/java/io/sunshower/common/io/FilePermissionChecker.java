package io.sunshower.common.io;

import io.sunshower.kernel.ObjectCheckException;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.val;

public class FilePermissionChecker {

  static final Logger log =
      Logger.getLogger("FilePermissions", "i18n.io.sunshower.kernel.launch.FilePermissions");

  private final List<Checker<File>> checkers;

  public FilePermissionChecker(final Checker<File>... checkers) {
    this.checkers = Arrays.asList(checkers);
  }

  public void check(@NonNull File file) throws ObjectCheckException {
    val iter = checkers.iterator();
    while (iter.hasNext()) {
      val check = iter.next();
      if (!check.check(file)) {
        val rb = log.getResourceBundle();
        val fmt = rb.getString("file.check.failed");
        val msg = MessageFormat.format(fmt, file, check);
        throw new ObjectCheckException(file, msg);
      }
    }
  }

  public enum Type implements Checker<File> {
    READ() {
      @Override
      boolean doCheck(File value) {
        return value.canRead();
      }
    },
    WRITE() {
      @Override
      boolean doCheck(File value) {
        return value.canWrite();
      }
    },
    EXECUTE() {
      @Override
      boolean doCheck(File value) {
        return value.canExecute();
      }
    },
    DELETE() {
      @Override
      boolean doCheck(File value) {
        val parent = value.getParentFile();
        if (parent != null) {
          return parent.canWrite();
        }
        return false;
      }
    };

    abstract boolean doCheck(File value);

    @Override
    public boolean check(File value) {
      if (value.exists()) {
        log.log(Level.FINE, "file.exists", value);
        if (doCheck(value)) {
          log.log(Level.FINE, "file.permission.ok", new Object[] {value, "READ"});
          return true;
        }
      } else {
        log.log(Level.WARNING, "file.notexists", value);
      }
      return false;
    }
  }
}
