package io.zephyr.common.io;

import io.zephyr.kernel.io.ChannelTransferListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class MonitorableByteChannel implements ReadableByteChannel {

  private final long expectedSize;
  private final ChannelTransferListener listener;
  private final ReadableByteChannel delegate;

  private long bytesRead;

  public MonitorableByteChannel(
      final ReadableByteChannel delegate,
      final ChannelTransferListener listener,
      long expectedSize) {
    this.listener = listener;
    this.delegate = delegate;
    this.expectedSize = expectedSize;
  }

  @Override
  public int read(ByteBuffer destination) throws IOException {
    int n = delegate.read(destination);
    if (n > 0) {
      bytesRead += n;
      double progress =
          expectedSize > 0 ? ((double) bytesRead / (double) expectedSize) * 100.0 : -1.0;
      listener.onTransfer(this, progress);
    }
    return n;
  }

  @Override
  public boolean isOpen() {
    return delegate.isOpen();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }
}
