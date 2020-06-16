package io.zephyr.barometer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.sunshower.test.common.Tests;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
@Disabled
public class ProcessIdProtocolTest {
  final Object lock = new Object();

  private File home;
  private ProcessIdProtocol protocol;
  private ExecutorService executorService;

  static final String myFile = "zephyr.pid";
  static final String theirFile = "agent.pid";

  @BeforeEach
  void setUp() {
    home = Tests.createTemp();
    executorService = Executors.newSingleThreadExecutor();
  }

  @Test
  void ensureProcessIdScanningWorks() throws InterruptedException {
    val listener = mock(ProcessIdListener.class);
    val pid = new AtomicLong();
    doAnswer(
            invocation -> {
              signal();
              pid.set(invocation.getArgument(0));
              return null;
            })
        .when(listener)
        .onProcessIdDiscovered(anyLong());
    protocol = create(listener);

    protocol.execute();
    val file = new File(home, theirFile);
    protocol.write(file, 1234);
    while (pid.get() == 0L) {
      synchronized (lock) {
        lock.wait(100);
      }
    }
    assertEquals(1234, pid.get());
    protocol.close();
  }

  @Test
  void ensureProcessIdFileIsRemoved() {
    val protocol = create(mock(ProcessIdListener.class));
    protocol.execute();
    assertTrue(new File(home, myFile).exists(), "my file must exist");
    protocol.close();
    assertFalse(new File(home, myFile).exists(), "my file must be disposed of");
  }

  private void signal() {
    synchronized (lock) {
      lock.notifyAll();
    }
  }

  private ProcessIdProtocol create(ProcessIdListener listener) {
    return new ProcessIdProtocol(home, myFile, theirFile, listener, executorService);
  }
}
