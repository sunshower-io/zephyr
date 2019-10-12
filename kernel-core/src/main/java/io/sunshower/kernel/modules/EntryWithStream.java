package io.sunshower.kernel.modules;

import java.io.InputStream;
import java.util.jar.JarEntry;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class EntryWithStream {
  final JarEntry entry;
  final InputStream inputStream;
}
