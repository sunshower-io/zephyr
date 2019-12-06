package io.zephyr.kernel.launch;

import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OutputArrayInputStream extends InputStream {

  private final List<String> messages;
  private volatile int read;
  private volatile int currentIdx;
  private volatile boolean closed;

  public OutputArrayInputStream(List<String> messages) {
    this.messages = messages;
    read = 0;
    closed = false;
    currentIdx = 0;
  }

  @Override
  public int read() throws IOException {
    synchronized (messages) {
      if (closed) {
        throw new IOException("Stream closed");
      }
      if (read < messages.size()) {
        val current = messages.get(read);
        if (currentIdx < current.length()) {
          return current.charAt(currentIdx++);
        }
        if (currentIdx == current.length()) {
          read++;
          currentIdx = 0;
        }
      }

      while (read == messages.size()) {
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
    read = 0;
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
