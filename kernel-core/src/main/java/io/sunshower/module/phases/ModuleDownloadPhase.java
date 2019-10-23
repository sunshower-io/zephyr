package io.sunshower.module.phases;

import io.sunshower.kernel.events.Event;
import io.sunshower.kernel.events.EventListener;
import io.sunshower.kernel.process.AbstractPhase;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.kernel.process.KernelProcessEvent;
import io.sunshower.kernel.process.Process;

public class ModuleDownloadPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {

  public ModuleDownloadPhase() {
    super(EventType.class);
  }

  public enum EventType implements KernelProcessEvent {
    OnDownloadStarted
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    super.doExecute(process, context);
    dispatch(new ModuleDownloadEvent());
  }

  public static class ModuleDownloadEvent
      implements Event<KernelProcessEvent, KernelProcessContext> {

    @Override
    public KernelProcessEvent getType() {
      return EventType.OnDownloadStarted;
    }
  }

  public static class ModuleDownloadEventListener
      implements EventListener<KernelProcessEvent, KernelProcessContext> {

    @Override
    public void onEvent(Event<KernelProcessEvent, KernelProcessContext> event) {}
  }
}
