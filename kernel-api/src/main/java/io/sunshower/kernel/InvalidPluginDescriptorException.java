package io.sunshower.kernel;

public class InvalidPluginDescriptorException extends ModuleException {
  public InvalidPluginDescriptorException() {}

  public InvalidPluginDescriptorException(String message) {
    super(message);
  }

  public InvalidPluginDescriptorException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidPluginDescriptorException(Throwable cause) {
    super(cause);
  }

  public InvalidPluginDescriptorException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
