package io.sunshower.kernel;

public class PluginLoadException extends PluginException {

  public PluginLoadException() {}

  public PluginLoadException(String message) {
    super(message);
  }

  public PluginLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginLoadException(Throwable cause) {
    super(cause);
  }

  public PluginLoadException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
