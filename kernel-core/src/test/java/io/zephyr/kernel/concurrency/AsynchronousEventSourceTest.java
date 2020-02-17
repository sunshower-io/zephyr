package io.zephyr.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.Events;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.DoNotUseThreads")
class AsynchronousEventSourceTest {

  private ExecutorService executorService;
  private AsynchronousEventSource eventSource;

  @BeforeEach
  void setUp() {
    executorService = Executors.newFixedThreadPool(2);
    eventSource = new AsynchronousEventSource(executorService);
  }

  @Test
  void ensureExecutorServiceDispatchWorks() {
    try {
      eventSource.start();
      eventSource.initialize();
      assertTrue(eventSource.isRunning(), "event source must be running");
    } finally {
      eventSource.stop();
    }
  }

  @Test
  void ensureAsyncDispatchWorks() {
    int timeout = 1000;
    EventListener<KernelEventTypes> longListener =
        (type, event) -> {
          try {
            Thread.sleep(timeout);
          } catch (InterruptedException ex) {
          }
        };
    try {
      eventSource.start();
      eventSource.initialize();
      eventSource.addEventListener(longListener, KernelEventTypes.KERNEL_START_FAILED);
      long t1 = System.currentTimeMillis();
      eventSource.dispatchEvent(KernelEventTypes.KERNEL_START_FAILED, Events.create(null));
      long t2 = System.currentTimeMillis();
      assertTrue(t2 - t1 < timeout, "Must not take a full second to dispatch");
    } finally {
      eventSource.stop();
    }
  }
}
