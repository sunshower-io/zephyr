package io.sunshower.kernel.log;

import java.util.logging.Logger;

public class Logging {

  public static Logger get(Class<?> logger) {
    return Logger.getLogger(logger.getName(), "i18n." + logger.getName());
  }

  /**
   * @param logger the class to log
   * @param rbSimpleName the simple name of the resource bundle to use
   * @return the logger attached to the resource-bundle at i18n/<code>logger.getPackage()</code>.
   *     <code>rbSimpleName</code>
   */
  public static Logger get(Class<?> logger, String rbSimpleName) {
    return Logger.getLogger(
        logger.getName(), "i18n." + logger.getPackageName() + "." + rbSimpleName);
  }
}
