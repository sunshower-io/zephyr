package io.sunshower.kernel.log;

import java.util.logging.*;
import java.util.logging.Logger;

import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.val;

public class DelegatingLoggingFacade implements io.sunshower.kernel.log.Logger {

  @Delegate private Logger logger;

  protected DelegatingLoggingFacade(Class<?> callingClass, String name, String resourceBundleName) {
    logger = doGetLogger(callingClass, name, resourceBundleName);
  }

  @SneakyThrows
  private static Logger doGetLogger(Class<?> callingClass, String name, String resourceBundleName) {
    val method = Logger.class.getDeclaredMethod("getLogger", String.class, String.class, Class.class);
    method.setAccessible(true);
    return (Logger) method.invoke(null, name, resourceBundleName, callingClass);
  }

  //  protected DelegatingLoggingFacade(Logger delegate) {
  //    this.logger = delegate;
  //  }

  @Override
  public void log(Level level, String msg, Object fst, Object... params) {
    if (!isLoggable(level)) {
      return;
    }
    // if params is empty, dog(level, msg, fst) would've been called instead

    val result = new Object[params.length + 1];
    result[0] = fst;
    System.arraycopy(params, 0, result, 1, params.length);
    log(level, msg, result);
  }
}
