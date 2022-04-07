package io.zephyr.kernel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import io.sunshower.lang.events.EventListener;
import io.sunshower.lang.events.EventType;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.kernel.KernelTestCase;
import io.zephyr.kernel.TaskQueue;
import io.zephyr.kernel.core.AbstractModule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServiceRegistryTestCase extends KernelTestCase {

  @Spy protected AbstractModule module;

  @Mock protected TaskQueue taskQueue;

  @Mock protected EventListener<?> listener;

  protected ServiceRegistry registry;
  protected List<EventListener<?>> listeners;

  @Override
  @BeforeEach
  protected void setUp() {

    listeners = new ArrayList<>();
    super.setUp();
    kernel.start();
    registry = kernel.getServiceRegistry();
    given(module.getTaskQueue()).willReturn(taskQueue);
  }

  @Override
  @AfterEach
  protected void tearDown() throws IOException {
    for (val listener : listeners) {
      kernel.removeEventListener(listener);
    }
    kernel.stop();
    assertEquals(0, kernel.getListenerCount(), "must have no more listeners");
    super.tearDown();
  }

  protected void registerListener(EventListener<?> listener, EventType... types) {
    kernel.addEventListener(listener, types);
    listeners.add(listener);
  }
}
