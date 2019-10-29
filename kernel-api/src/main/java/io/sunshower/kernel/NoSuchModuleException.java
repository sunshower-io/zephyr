package io.sunshower.kernel;

public class NoSuchModuleException extends ModuleException {
  private final Coordinate coordinate;

  public NoSuchModuleException(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  public NoSuchModuleException(String message, Coordinate coordinate) {
    super(message);
    this.coordinate = coordinate;
  }

  public NoSuchModuleException(String message, Throwable cause, Coordinate coordinate) {
    super(message, cause);
    this.coordinate = coordinate;
  }

  public NoSuchModuleException(Throwable cause, Coordinate coordinate) {
    super(cause);
    this.coordinate = coordinate;
  }

  public NoSuchModuleException(
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      Coordinate coordinate) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.coordinate = coordinate;
  }
}
