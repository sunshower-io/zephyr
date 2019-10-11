package io.sunshower.common.io;

import static io.sunshower.common.io.FileNames.nameOf;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FileNamesTest {

  @Test
  void ensureFileNameReturnsNullForNull() {
    assertNull(nameOf(null));
  }

  @Test
  void ensureNameOfReturnsNameSansExtensionForExtension() {
    assertEquals(nameOf("test.txt"), "test");
  }

  @Test
  void ensureFileNameReturnsNameForNameNoExt() {
    assertEquals(nameOf("name"), "name");
  }
}
