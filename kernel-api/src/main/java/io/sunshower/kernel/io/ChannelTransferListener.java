package io.sunshower.kernel.io;

import java.nio.channels.ReadableByteChannel;

public interface ChannelTransferListener {

  default void onTransfer(ReadableByteChannel channel, double progress) {}

  default void onComplete(ReadableByteChannel channel) {}

  default void onError(ReadableByteChannel channel, Exception ex) {}

  default void onCancel(ReadableByteChannel channel) {}
}
