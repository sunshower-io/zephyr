package io.zephyr.kernel.service;

import io.zephyr.api.ServiceRegistry;
import io.zephyr.kernel.KernelTestCase;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DefaultServiceRegistryTest extends KernelTestCase {

  private       ServiceRegistry  registry;
  @Mock private EventListener<?> listener;
  private List<EventListener<?>> listeners;

  @BeforeEach
  protected void setUp() {
    listeners = new ArrayList<>();
    super.setUp();
    kernel.start();
    registry = kernel.getServiceRegistry();
  }

  @AfterEach
  protected void tearDown() throws IOException {
    for (val listener : listeners) {
      kernel.removeEventListener(listener);
    }
    kernel.stop();
    assertEquals(0, kernel.getListenerCount(), "must have no more listeners");
    super.tearDown();
  }

  @Test
  void ensureRegistryDispatchesCorrectEventForServiceRegistration() {
    registerListener(listener, ServiceEvents.);
    registry.register(new DefaultServiceDefinition<>(String.class, "hello"));
  }

  private void registerListener(EventListener<?> listener, EventType... types) {
    kernel.addEventListener(listener, types);
    listeners.add(listener);
  }
}
