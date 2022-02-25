package io.sunshower.test.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import lombok.extern.java.Log;
import lombok.val;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

@Log
public class ThreadLockFailedExtension implements TestWatcher,
    AfterEachCallback {

  @Override
  public void afterEach(ExtensionContext extensionContext) throws Exception {

  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    log.log(Level.SEVERE, "Test {0} failed while the JVM was holding the following locks",
        context.getDisplayName());
    Tests.dumpAllThreadLocks(System.err);


  }
}
