package io.sunshower.common.io;

import java.io.*;

public class IO {

  public static void copy(final InputStream in, final OutputStream out) throws IOException {
    byte[] buf = new byte[16384];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    out.flush();
  }
}
