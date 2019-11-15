package io.sunshower.kernel.shell;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.events.Event;
import io.sunshower.kernel.events.EventListener;
import io.sunshower.kernel.events.EventType;
import io.sunshower.kernel.launch.KernelOptions;
import javax.inject.Inject;
import lombok.ToString;

import java.util.concurrent.Callable;

@ToString
public abstract class Command implements Callable<Integer>, EventListener<Object> {

  @Inject protected Kernel kernel;

  @Inject protected ShellConsole console;

  @Inject protected KernelOptions options;

  private static final EventType[] EMPTY_ARRAY = new EventType[0];

  protected final EventType[] eventTypes;

  protected Command(EventType... types) {
    this.eventTypes = types;
  }

  protected Command() {
    this(EMPTY_ARRAY);
  }

  protected EventType[] getEvents() {
    return eventTypes;
  }

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

  public void onEvent(EventType type, Event<Object> event) {}
}
