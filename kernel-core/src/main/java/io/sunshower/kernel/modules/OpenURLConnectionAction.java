package io.sunshower.kernel.modules;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.PrivilegedExceptionAction;

public class OpenURLConnectionAction implements PrivilegedExceptionAction<URLConnection> {

  private final URL url;

  OpenURLConnectionAction(final URL url) {
    this.url = url;
  }

  public URLConnection run() throws IOException {
    final URLConnection c = url.openConnection();
    c.connect();
    return c;
  }
}
