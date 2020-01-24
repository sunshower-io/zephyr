package io.zephyr.kernel.io;

import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class ObservableChannelTransferListener implements ChannelTransferListener {

  private final Object lock = new Object();

  private final List<ChannelTransferListener> listeners;

  public ObservableChannelTransferListener() {
    listeners = new ArrayList<>();
  }

  public void addListener(ChannelTransferListener listener) {
    synchronized (lock) {
      listeners.add(listener);
    }
  }

  public void removeListener(ChannelTransferListener listener) {
    synchronized (lock) {
      listeners.removeIf(t -> t == listener);
    }
  }

  @Override
  public void onTransfer(ReadableByteChannel channel, double progress) {
    synchronized (lock) {
      for (ChannelTransferListener listener : listeners) {
        listener.onTransfer(channel, progress);
      }
    }
  }

  @Override
  public void onComplete(ReadableByteChannel channel) {
    synchronized (lock) {
      for (ChannelTransferListener listener : listeners) {
        listener.onComplete(channel);
      }
    }
  }

  @Override
  public void onError(ReadableByteChannel channel, Exception ex) {
    synchronized (lock) {
      for (ChannelTransferListener listener : listeners) {
        listener.onError(channel, ex);
      }
    }
  }

  @Override
  public void onCancel(ReadableByteChannel channel) {
    synchronized (lock) {
      for (ChannelTransferListener listener : listeners) {
        listener.onCancel(channel);
      }
    }
  }
}
