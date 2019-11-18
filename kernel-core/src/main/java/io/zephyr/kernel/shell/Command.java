package io.zephyr.kernel.shell;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.launch.KernelOptions;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import lombok.ToString;

@ToString
public abstract class Command implements Callable<Integer>, EventListener<Object> {

  @Inject protected Kernel kernel;

  @Inject protected ShellConsole console;

  @Inject protected KernelOptions options;

  private static final EventType[] EMPTY_ARRAY = new EventType[0];

  protected final EventType[] eventTypes;

  @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  protected Command(EventType... types) {
    this.eventTypes = types;
  }

  protected Command() {
    this(EMPTY_ARRAY);
  }

  @SuppressWarnings("unchecked")
  public Command(Class enumType) {
    if (!Enum.class.isAssignableFrom(enumType)) {
      throw new IllegalStateException("Can't use this constructor with a non-enum type");
    }
    EnumSet results = EnumSet.allOf((Class<Enum>) enumType);
    eventTypes = (EventType[]) results.toArray(new EventType[0]);
  }

  @SuppressWarnings("PMD.MethodReturnsInternalArray")
  protected EventType[] getEvents() {
    return eventTypes;
  }

  @Override
  public final Integer call() {
    try {
      kernel.addEventListener(this, getEvents());
      return execute();
    } finally {
      kernel.removeEventListener(this);
    }
  }

  protected int execute() {
    return -1;
  }

  @Override
  public void onEvent(EventType type, Event<Object> event) {}
}
