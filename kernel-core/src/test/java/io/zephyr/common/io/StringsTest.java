package io.zephyr.common.io;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StringsTest {

  @Test
  void ensureStringsExistsWorksForNull() {
    assertTrue(Strings.isNullOrEmpty(null), "null string must not exist");
  }

  @Test
  void ensureStringsExistsWorksForBlankNonNull() {
    assertTrue(Strings.isNullOrEmpty("    "), "blank string must not exist");
  }
}
