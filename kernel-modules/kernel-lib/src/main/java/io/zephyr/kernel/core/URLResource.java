package io.zephyr.kernel.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import lombok.Data;
import org.jboss.modules.Resource;

@Data
public class URLResource implements Resource {

  final URL url;

  @Override
  public String getName() {
    return url.toExternalForm();
  }

  @Override
  public URL getURL() {
    return url;
  }

  @Override
  public InputStream openStream() throws IOException {
    return url.openStream();
  }

  @Override
  public long getSize() {
    return 0;
  }
}
