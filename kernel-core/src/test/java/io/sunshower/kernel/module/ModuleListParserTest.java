package io.sunshower.kernel.module;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.module.phases.AbstractModulePhaseTestCase;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.EmptyCatchBlock",
  "PMD.JUnitUseExpected",
  "PMD.AvoidDuplicateLiterals",
  "PMD.UseProperClassLoader",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage",
})
class ModuleListParserTest extends AbstractModulePhaseTestCase {

  @Test
  void ensureParsingOrderWorks() throws IOException {
    val position = new ModuleListParser.Position();
    val is = s("123:");
    val result = ModuleListParser.parseOrder(is, position);
    assertEquals(result, 123, "parsed value must be correct");
    assertEquals(position.pos, 2, "parser position must be correct");
    assertEquals((char) is.read(), ':', "inputstream must be in correct position");
  }

  @Test
  void ensureParsingCompleteLineWorksSansLibDirectoriesWorks() throws IOException {
    val test = s("1:io.sunshower:sunshower-whatever:1.0.0-SNAPSHOT");
    val position = new ModuleListParser.Position();
    val items = new ArrayList<KernelModuleEntry>();
    ModuleListParser.doParse(test, items, position);
    assertEquals(items.size(), 1, "must have one entry in it");
    val result = items.get(0);
    assertEquals(result.getOrder(), 1);
    assertEquals(result.getGroup(), "io.sunshower");
    assertEquals(result.getName(), "sunshower-whatever");
    assertEquals(result.getVersion(), "1.0.0-SNAPSHOT");
    assertTrue(result.getLibraryFiles().isEmpty());
  }

  @Test
  void ensureWhateverWorks() throws IOException {
    try (val fs =
        new PushbackInputStream(ClassLoader.getSystemResourceAsStream("modules/modules.list"))) {
      val list = new ArrayList<KernelModuleEntry>();
      val pos = new ModuleListParser.Position();
      ModuleListParser.doParse(fs, list, pos);
      assertEquals(list.size(), 5);
    }
  }

  @Test
  void ensureParsingCompleteLineWithLibraryFilesWorks() throws IOException {
    val test = s("1:io.sunshower:sunshower-whatever:1.0.0-SNAPSHOT[lib/dapper,frapper,dapper]");
    val pos = new ModuleListParser.Position();
    val items = new ArrayList<KernelModuleEntry>();
    ModuleListParser.doParse(test, items, pos);
    val result = items.get(0);
    assertEquals(
        new HashSet<>(result.getLibraryFiles()), Set.of("lib/dapper", "frapper", "dapper"));
  }

  PushbackInputStream s(String value) {
    return new PushbackInputStream(
        new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
  }
}
