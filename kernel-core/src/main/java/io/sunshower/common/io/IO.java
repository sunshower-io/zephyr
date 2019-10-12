package io.sunshower.common.io;

import java.io.*;
import java.util.zip.ZipEntry;

public class IO {

  public static void copy(final InputStream in, final OutputStream out) throws IOException {
    byte[] buf = new byte[16384];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    out.flush();
  }

  public static InputStream copyStream(InputStream in, ZipEntry entry) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    long size = entry.getSize();
    if (size > -1) {
      byte[] buffer = new byte[1024 * 4];
      int n = 0;
      long count = 0;
      while (-1 != (n = in.read(buffer)) && count < size) {
        baos.write(buffer, 0, n);
        count += n;
      }
    } else {
      while (true) {
        int b = in.read();
        if (b == -1) {
          break;
        }
        baos.write(b);
      }
    }
    baos.close();
    return new ByteArrayInputStream(baos.toByteArray());
  }
}
