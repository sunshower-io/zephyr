package io.sunshower.kernel.modules;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.jboss.modules.Resource;

public final class EntryResource implements Resource {
  private final JarFile jarFile;
  private final String relativePath;
  private final String entryName;
  private final URL resourceURL;

  public EntryResource(
      final JarFile jarFile, final String name, final String relativePath, final URL resourceURL) {
    this.jarFile = jarFile;
    this.relativePath = relativePath;
    this.entryName = relativePath == null ? name : name.substring(relativePath.length() + 1);
    this.resourceURL = resourceURL;
  }

  public String getName() {
    return entryName;
  }

  public URL getURL() {
    return resourceURL;
  }

  public InputStream openStream() throws IOException {
    return jarFile.getInputStream(getEntry());
  }

  public long getSize() {
    final long size = getEntry().getSize();
    return size == -1 ? 0 : size;
  }

  private ZipEntry getEntry() {
    final String relativePath = this.relativePath;
    return relativePath == null
        ? jarFile.getEntry(entryName)
        : jarFile.getEntry(relativePath + "/" + entryName);
  }
}
