package flywaytest;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import lombok.SneakyThrows;
import org.flywaydb.core.api.resource.LoadableResource;

public class UrlResourceLoadableResource extends LoadableResource {

  private final URL url;

  public UrlResourceLoadableResource(URL classLoaderUrl) {
    this.url = classLoaderUrl;
  }

  @SneakyThrows
  @Override
  public Reader read() {
    return new InputStreamReader(this.url.openStream());
  }

  @Override
  public String getAbsolutePath() {
    return url.toExternalForm();
  }

  @Override
  public String getAbsolutePathOnDisk() {
    return url.toExternalForm();
  }

  @Override
  public String getFilename() {
    String[] segs = url.getFile().split(File.separator);
    return segs[segs.length - 1];
  }

  @Override
  public String getRelativePath() {
    return url.getPath();
  }
}
