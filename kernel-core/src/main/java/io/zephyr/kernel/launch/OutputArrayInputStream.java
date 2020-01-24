package io.zephyr.kernel.launch;

import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.val;

@SuppressFBWarnings
@SuppressWarnings("PMD.AvoidUsingVolatile")
public class OutputArrayInputStream extends InputStream {

  private final List<String> messages;
  private volatile int readCount;
  private volatile int currentIdx;
  private volatile boolean closed;

  public OutputArrayInputStream(List<String> messages) {
    this.messages = messages;
    readCount = 0;
    closed = false;
    currentIdx = 0;
  }

  @Override
  public int read() throws IOException {
    synchronized (messages) {
      if (closed) {
        throw new IOException("Stream closed");
      }
      if (readCount < messages.size()) {
        val current = messages.get(readCount);
        if (currentIdx < current.length()) {
          return current.charAt(currentIdx++);
        }
        if (currentIdx == current.length()) {
          readCount++;
          currentIdx = 0;
        }
      }

      while (readCount == messages.size()) {
        try {
          messages.wait(100);
        } catch (InterruptedException ex) {
          throw new IOException(ex);
        }
      }
      return read();
    }
  }

  @Override
  public void reset() {
    readCount = 0;
    currentIdx = 0;
  }

  @Override
  public void close() throws IOException {
    synchronized (messages) {
      closed = true;
      messages.notifyAll();
    }
  }
}
