package io.sunshower.common.io;

import static io.sunshower.common.io.MonitorableChannels.from;
import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.io.ChannelTransferListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.val;
import org.junit.jupiter.api.Test;

class MonitorableFileTransferTest {

  @Test
  void ensureTransferringWorks() throws IOException {
    val called = new AtomicBoolean();
    from(
            ClassLoader.getSystemResource("io/files/test.txt"),
            new ChannelTransferListener() {
              @Override
              public void onTransfer(ReadableByteChannel channel, double progress) {
                called.set(true);
              }
            })
        .read(ByteBuffer.allocate(10));
    assertTrue(called.get(), "onTransfer must be called");
  }
}
