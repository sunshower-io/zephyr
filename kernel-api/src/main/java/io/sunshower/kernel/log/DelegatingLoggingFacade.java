package io.sunshower.kernel.log;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class DelegatingLoggingFacade extends Logger implements io.sunshower.kernel.log.Logger {
  protected DelegatingLoggingFacade(String name, String resourceBundleName) {
    super(name, resourceBundleName);
  }

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
