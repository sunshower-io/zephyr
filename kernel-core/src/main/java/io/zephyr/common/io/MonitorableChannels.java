package io.zephyr.common.io;

import io.zephyr.kernel.io.ChannelTransferListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import lombok.val;

public class MonitorableChannels {

  public static ReadableByteChannel from(URL url, ChannelTransferListener listener)
      throws IOException {
    val connection = url.openConnection();
    connection.connect();
    val stream = connection.getInputStream();
    val len = connection.getContentLengthLong();
    return new MonitorableByteChannel(Channels.newChannel(stream), listener, len);
  }

  public static MonitorableFileTransfer transfer(URL url, Path path) throws IOException {
    return transfer(url, path.toFile());
  }

  public static MonitorableFileTransfer transfer(URL url, File destination) throws IOException {
    val connection = url.openConnection();
    connection.connect();
    val stream = connection.getInputStream();
    val len = connection.getContentLengthLong();
    return new MonitorableFileTransfer(destination, len, Channels.newChannel(stream));
  }
}
