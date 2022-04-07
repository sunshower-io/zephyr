package io.sunshower.lang.events;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import io.zephyr.api.ModuleEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class AbstractEventSourceTest {
  AbstractEventSource source;
  @Mock EventListener<?> listener;

  @BeforeEach
  void setUp() {
    source = new AbstractEventSource();
    listener = mock(EventListener.class);
  }

  @Test
  void ensureListenerSetWithNoOptionsIsNotRemovedAfterDispatch() {
    source.addEventListener(listener, EventListener.Options.NONE, ModuleEvents.STARTED);

    assertEquals(1, source.listeners.size(), "must have listener");
    assertTrue(source.listensFor(ModuleEvents.STARTED), "must contain event listener");
    source.dispatchEvent(ModuleEvents.STARTED, mock(Event.class));

    assertEquals(1, source.listeners.size(), "must have same listener");
    assertTrue(source.listensFor(ModuleEvents.STARTED), "must contain event listener");
  }

  @Test
  void ensureAddingListenerIncrementsSize() {
    source.addEventListener(
        listener, EventListener.Options.REMOVE_AFTER_DISPATCH, ModuleEvents.STARTED);

    assertEquals(source.listeners.size(), 1);
    assertTrue(source.listensFor(ModuleEvents.STARTED), "must contain event listener");
    source.dispatchEvent(ModuleEvents.STARTED, mock(Event.class));
    assertFalse(source.listensFor(ModuleEvents.STARTED), "must not contain event listener");
    assertTrue(source.listeners.isEmpty(), "must contain no listeners");
  }

  @Test
  void ensureListenerDoesNotListenForNonExistantEvents() {
    assertFalse(
        source.listensFor(ModuleEvents.STARTED),
        "must not listen for event type that has not been subscribed to");
  }
}
