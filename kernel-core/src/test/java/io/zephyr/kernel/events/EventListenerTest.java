package io.sunshower.lang.events;

import static io.sunshower.lang.events.EventListener.Options.isSet;
import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class EventListenerTest {

  @Test
  void ensureRemoveAfterDispatchIsNotSetByDefault() {
    assertFalse(isSet(EventListener.Options.REMOVE_AFTER_DISPATCH), "must not be set");
  }

  @Test
  void ensureSettingRemoveAfterDispatchWorks() {
    val flag = EventListener.Options.set(EventListener.Options.REMOVE_AFTER_DISPATCH);
    assertTrue(isSet(EventListener.Options.REMOVE_AFTER_DISPATCH, flag), "must be set");
  }
}
