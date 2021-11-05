package io.zephyr.kernel.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.core.SemanticVersion;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals",
    "PMD.JUnitTestContainsTooManyAsserts",
    "PMD.JUnitAssertionsShouldIncludeMessage"
})
class ManifestModuleScannerTest {

  ManifestModuleScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new ManifestModuleScanner();
  }

  @ParameterizedTest
  @SneakyThrows
  @ValueSource(strings = {"library", "service"})
  void ensureReadingSingleDependencyWithFullDependencySpecWorks(String value) {
    val reader = new PushbackReader(new StringReader(value));
    assertEquals(Dependency.Type.parse(value), scanner.readDependencyType(reader));
  }

  @Test
  @SneakyThrows
  void ensureReadingSmallerMatchingStringWorks() {
    val reader = new PushbackReader(new StringReader("a"));
    val result = scanner.expectOneOf(reader, "a", "cd");
    assertEquals("a", result);
  }

  @Test
  @SneakyThrows
  void ensureReadingLargerMatchingStringWorks() {
    val reader = new PushbackReader(new StringReader("cd"));
    val result = scanner.expectOneOf(reader, "a", "cd");
    assertEquals("cd", result);
  }

  @Test
  @SneakyThrows
  void ensureNonMatchingStringThrowsCorrectException() {
    val reader = new PushbackReader(new StringReader("hello"));
    assertThrows(IllegalArgumentException.class, () -> {
      scanner.expectOneOf(reader, "a", "cd");
    });
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r\n", "\r"})
  void ensureReadingSimpleDependencyWorks(String linefeed) {
    val coordinate = "service@hello:world:1.0.0-SNAPSHOT" + linefeed;
    val reader = readerFor(coordinate);
    val dependencies = new ArrayList<Dependency>();
    scanner.readDependency(dependencies, reader);
    assertEquals(1, dependencies.size(), "must have one dependency");
    val dep = dependencies.get(0);
    val coord = dep.getCoordinate();
    assertEquals(coord.getGroup(), "hello");
    assertEquals(coord.getName(), "world");
    assertEquals(coord.getVersion(), new SemanticVersion("1.0.0-SNAPSHOT"));
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r\n", "\r"})
  void ensureCommaSeparatedDependenciesWork(String linefeed) {

    val coordinate =
        "service@hello0:world:1.0.0-SNAPSHOT,"
        + "service@whatever.world1:coolbeans:1.0.0-Final  , "
        + "service@whatever.world2:coolbeans:1.0.0-Final,   "
        + "service@whatever.world3:coolbeans:1.0.0-Final"
        + linefeed;
    val reader = readerFor(coordinate);
    val deps = scanner.readDependencies(reader);
    assertEquals(4, deps.size());
    for (int i = 0; i < deps.size(); i++) {
      assertTrue(deps.get(i).getCoordinate().getGroup().contains("" + i), "must contain " + i);
    }
  }

  @Test
  @SneakyThrows
  void ensureOptionalIsParsed() {
    val coordinate =
        String.format("service@hello0:world:1.0.0-SNAPSHOT<optional>");

    val reader = readerFor(coordinate);
    val deps = scanner.readDependencies(reader);
    assertEquals(1, deps.size());
    val dependency = deps.get(0);
    assertTrue(dependency.isOptional());


  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r\n", "\r", ""})
  void ensureOptionalWorksForSingleDependency(String linefeed) {
    val leading = new String[] {
        "",
        "\t",
        " ",
        " \t",
        " \t ",
    };

    val trailing= new String[] {
        "",
        "\t",
        " ",
        " \t",
        " \t ",
    };

    for(val l : leading) {
      for(val t : trailing) {

        val coordinate =
            String.format("service@hello0:world:1.0.0-SNAPSHOT<%soptional%s>%s", l, t, linefeed);
        val reader = readerFor(coordinate);
        val deps = scanner.readDependencies(reader);
        assertEquals(1, deps.size());
        assertTrue(deps.get(0).isOptional());
      }
    }
  }
  private PushbackReader readerFor(String s) {
    return new PushbackReader(new StringReader(s));

  }
}
