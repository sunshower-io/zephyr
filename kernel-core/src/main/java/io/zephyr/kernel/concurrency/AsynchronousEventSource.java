package io.zephyr.kernel.concurrency;

import io.zephyr.api.Startable;
import io.zephyr.api.Stoppable;
import io.zephyr.kernel.events.*;
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
  public <T> void addEventListener(EventListener<T> listener, EventType... types) {
    synchronized (source) {
      source.addEventListener(listener, types);
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
          wait(200);
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
