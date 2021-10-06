package io.zephyr.support.flyway;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.Getter;
import lombok.val;
import org.flywaydb.core.api.resource.LoadableResource;

public class ModuleLoadableResource extends LoadableResource {
  final byte[] data;
  private final String name;
  @Getter final String absolutePath;
  @Getter final String absolutePathOnDisk;

  public ModuleLoadableResource(File assemblyFile, ZipFile file, ZipEntry entry, String location)
      throws IOException {
    val outputstream = new ByteArrayOutputStream();
    try (val inputStream = file.getInputStream(entry)) {
      inputStream.transferTo(outputstream);
      data = outputstream.toByteArray();
    }
    val segs = entry.getName().split(Pattern.quote(File.separator));
    this.name = segs[segs.length - 1];
    this.absolutePath = assemblyFile.getAbsolutePath() + "!" + location;
    this.absolutePathOnDisk = absolutePath;
  }

  @Override
  public Reader read() {
    return new InputStreamReader(new ByteArrayInputStream(data));
  }

  @Override
  public String getFilename() {
    return name;
  }

  @Override
  public String getRelativePath() {
    return getFilename();
  }
}
