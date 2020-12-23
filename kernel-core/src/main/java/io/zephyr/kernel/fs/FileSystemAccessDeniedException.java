package io.zephyr.kernel.fs;

public class FileSystemAccessDeniedException extends ModuleFileSystemException {

  static final long serialVersionUID = 583932134230223117L;

  public FileSystemAccessDeniedException() {
    super();
  }

  public FileSystemAccessDeniedException(String message) {
    super(message);
  }

  public FileSystemAccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileSystemAccessDeniedException(Throwable cause) {
    super(cause);
  }

  protected FileSystemAccessDeniedException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
