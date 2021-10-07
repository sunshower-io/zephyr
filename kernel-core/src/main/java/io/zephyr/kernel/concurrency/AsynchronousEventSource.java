package io.zephyr.kernel.concurrency;

import io.zephyr.api.Startable;
import io.zephyr.api.Stoppable;
import io.zephyr.kernel.events.AbstractEventSource;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventSource;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import lombok.AllArgsConstructor;
import lombok.val;

@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.AvoidUsingVolatile"})
public class AsynchronousEventSource implements EventSource, Stoppable, Startable {

  private final QueuedEventSource source;
  private final ExecutorService executorService;
  private final TransferQueue<AsynchronousEvent<?>> queue;

  private final Object queueLock = new Object();

  public AsynchronousEventSource(ExecutorService executorService) {
    queue = new LinkedTransferQueue<>();
    source = new QueuedEventSource();
    this.executorService = executorService;
  }

  public boolean isRunning() {
    return source.running;
  }

  @Override
  public void stop() {
    source.stop();
  }

  @Override
  public int getListenerCount() {
    return source.getListenerCount();
  }

  @Override
  public boolean listensFor(EventType... types) {
    synchronized (source) {
      return source.listensFor(types);
    }
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, EventType... types) {
    addEventListener(listener, EventListener.Options.NONE, types);
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, int options, EventType... types) {
    synchronized (source) {
      source.addEventListener(listener, options, types);
    }
  }

  @Override
  public <T> void removeEventListener(EventListener<T> listener) {
    synchronized (source) {
      source.removeEventListener(listener);
    }
  }

  @Override
  public <T> void dispatchEvent(EventType type, Event<T> event) {
    synchronized (queueLock) {
      queue.add(new AsynchronousEvent<>(event, type));
      queueLock.notifyAll();
    }
  }

  @Override
  public void start() {
    synchronized (this) {
      executorService.submit(source);
    }
  }

  /** waits for initialization */
  public void initialize() {
    synchronized (this) {
      while (!source.running) {
        try {
          wait();
        } catch (InterruptedException ex) {
        }
      }
    }
  }

  @AllArgsConstructor
  static class AsynchronousEvent<T> {

    final Event<T> event;
    final EventType eventType;
  }

  final class QueuedEventSource extends AbstractEventSource implements Runnable, Stoppable {

    volatile boolean running;

    @Override
    public void run() {
      synchronized (queueLock) {
        running = true;
        synchronized (AsynchronousEventSource.this) {
          AsynchronousEventSource.this.notifyAll();
        }
        while (running) {
          while (queue.isEmpty()) {
            try {
              queueLock.wait();
            } catch (InterruptedException ex) {
              return;
            }
          }
          drain();
        }
      }
    }

    @SuppressFBWarnings
    private void drain() {
      while (!queue.isEmpty()) {
        val next = queue.remove();
        QueuedEventSource.this.dispatchEvent(next.eventType, next.event);
      }
    }

    @Override
    public void stop() {
      synchronized (queueLock) {
        running = false;
        queueLock.notifyAll();
      }
    }
  }
}
