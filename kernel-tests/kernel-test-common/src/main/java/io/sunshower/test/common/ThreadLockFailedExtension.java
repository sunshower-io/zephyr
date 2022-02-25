package io.sunshower.test.common;

import java.util.logging.Level;
import lombok.extern.java.Log;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

@Log
public class ThreadLockFailedExtension implements TestWatcher, AfterEachCallback {

  @Override
  public void afterEach(ExtensionContext extensionContext) throws Exception {}

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    log.log(
        Level.SEVERE,
        "Test {0} failed while the JVM was holding the following locks",
        context.getDisplayName());
    Tests.dumpAllThreadLocks(System.err);
  }
}
