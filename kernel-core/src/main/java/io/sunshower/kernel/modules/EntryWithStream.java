package io.sunshower.kernel.modules;

import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.util.jar.JarEntry;

@AllArgsConstructor
public final class EntryWithStream {
  final JarEntry entry;
  final InputStream inputStream;
}
