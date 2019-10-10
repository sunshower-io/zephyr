package io.sunshower.common.io;

import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.io.ObservableChannelTransferListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.*;
import lombok.val;

public class MonitorableFileTransfer extends ObservableChannelTransferListener
    implements Callable<File>, ChannelTransferListener {

  private final long expectedSize;
  private final File destination;
  private final ReadableByteChannel channel;

  public MonitorableFileTransfer(File destination, long expectedSize, ReadableByteChannel channel) {
    this.channel = channel;
    this.destination = destination;
    this.expectedSize = expectedSize;
  }

  @Override
  public File call() throws Exception {
    Exception e = null;
    val source = new MonitorableByteChannel(channel, this, expectedSize);
    try (val outputStream = new FileOutputStream(destination);
        val destination = outputStream.getChannel()) {
      destination.transferFrom(source, 0, Long.MAX_VALUE);
    } catch (ClosedChannelException ex) {
      e = ex;
      onCancel(channel);
    } catch (IOException ex) {
      e = ex;
      onError(channel, ex);
    }
    if (e != null) {
      throw e;
    }
    onComplete(channel);
    return destination;
  }
}
