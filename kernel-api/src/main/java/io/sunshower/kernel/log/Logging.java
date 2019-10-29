package io.sunshower.kernel.log;

import lombok.val;

public class Logging {
  private Logging() {}

  public static Logger get(Class<?> logger) {
    val name = logger.getName();
    return new DelegatingLoggingFacade(name, "i18n." + name);
  }

  /**
   * @param logger the class to log
   * @param rbSimpleName the simple name of the resource bundle to use
   * @return the logger attached to the resource-bundle at i18n/<code>logger.getPackage()</code>.
   *     <code>rbSimpleName</code>
   */
  public static Logger get(Class<?> logger, String rbSimpleName) {
    val name = logger.getName();
    val pkg = logger.getPackageName();
    return new DelegatingLoggingFacade(name, "i18n." + pkg + "." + rbSimpleName);
  }
}
