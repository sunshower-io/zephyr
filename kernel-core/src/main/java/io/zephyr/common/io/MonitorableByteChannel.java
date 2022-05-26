package io.zephyr.common.io;

import io.zephyr.kernel.io.ChannelTransferListener;
import java.io.IOException;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import lombok.extern.java.Log;
import lombok.val;

@Log
public class MonitorableByteChannel implements ReadableByteChannel {

  private final long expectedSize;
  private final URLConnection connection;
  private final ReadableByteChannel delegate;
  private final ChannelTransferListener listener;

  private long bytesRead;

  public MonitorableByteChannel(
      URLConnection connection,
      final ReadableByteChannel delegate,
      final ChannelTransferListener listener,
      long expectedSize) {
    this.listener = listener;
    this.delegate = delegate;
    this.connection = connection;
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
    try {
      if (connection instanceof HttpURLConnection) {
        ((HttpURLConnection) connection).disconnect();
      } else {
        forceClose(connection);
      }
    } finally {
      delegate.close();
    }
  }

  private void forceClose(URLConnection connection) {
    forceClose(connection, "close");
    forceClose(connection, "disconnect");
  }

  private void forceClose(URLConnection connection, String disconnect) {
    for (Class<?> c = connection.getClass(); !Object.class.equals(c); c = c.getSuperclass()) {
      try {
        val method = c.getDeclaredMethod(disconnect);
        method.setAccessible(true);
        method.invoke(connection);
        log.log(Level.INFO, "Successfully closed connection");
        return;
      } catch (NoSuchMethodException | InaccessibleObjectException ex) {
        log.log(Level.FINEST, "No close method or inaccessible close method on {0}", c);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
