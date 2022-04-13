package io.zephyr.kernel.module;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.Dependency.ServicesResolutionStrategy;
import io.zephyr.kernel.core.PathSpecification.Mode;
import io.zephyr.kernel.core.SemanticVersion;
import java.io.IOException;
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
  void ensureReadingOrderWorks() {
    val a = "service@io.zephyr:zephyr:1.0.0-Final<order=40>";
    val deps = scanner.readDependencies(readerFor(a));
    assertEquals(1, deps.size());
    val dep = deps.get(0);
    assertEquals(40, dep.getOrder());
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
    assertThrows(
        IllegalArgumentException.class,
        () -> {
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
  void ensureSemanticVersionWorks() {}

  @Test
  @SneakyThrows
  void ensureOptionalIsParsed() {
    val coordinate = format("service@hello0:world:1.0.0-SNAPSHOT<optional>");

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
    val leading =
        new String[] {
          "", "\t", " ", " \t", " \t ",
        };

    val trailing =
        new String[] {
          "", "\t", " ", " \t", " \t ",
        };

    for (val l : leading) {
      for (val t : trailing) {

        val coordinate =
            format("service@hello0:world:1.0.0-SNAPSHOT<%soptional%s>%s", l, t, linefeed);
        val reader = readerFor(coordinate);
        val deps = scanner.readDependencies(reader);
        assertEquals(1, deps.size());
        assertTrue(deps.get(0).isOptional());
      }
    }
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"none", "import", "export"})
  void ensureServicesWorksForAllValuesWithLeadingAndTrailingWhitespace(String linefeed) {
    val leading =
        new String[] {
          "", "\t", " ", " \t", " \t ",
        };

    val trailing =
        new String[] {
          "", "\t", " ", " \t", " \t ",
        };

    for (val l : leading) {
      for (val t : trailing) {

        val coordinate =
            format("service@hello0:world:1.0.0-SNAPSHOT<%sservices=%s%s%s>", l, t, linefeed, l);
        System.out.println(coordinate);
        val reader = readerFor(coordinate);
        val deps = scanner.readDependencies(reader);
        assertEquals(1, deps.size());
        assertEquals(
            ServicesResolutionStrategy.parse(linefeed),
            deps.get(0).getServicesResolutionStrategy());
      }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"all", "just"})
  void ensureReadPathSpecificationCanBeReadForImports(String value) throws IOException {
    val coord =
        readerFor(
            format("service@hello0:world:1.0.0-SNAPSHOT<imports-paths=[%s:hello/world]>", value));
    val results = scanner.readDependencies(coord);
    assertEquals(results.size(), 1);
    val imports = results.get(0).getImports();
    assertEquals(imports.size(), 1);
    val i = imports.get(0);
    assertEquals(i.getPath(), "hello/world");
    assertEquals(i.getMode(), Mode.parse(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {"exports"})
  void ensureReadPathSpecificationCanBeReadForMultipleExports(String type) throws IOException {
    val coord =
        readerFor(
            format(
                "service@hello0:world:1.0.0-SNAPSHOT<%s-paths=[just : hello/world, all  :   world/world ]>",
                type));
    val results = scanner.readDependencies(coord);
    assertEquals(results.size(), 1);
    val exports = results.get(0).getExports();
    assertEquals(exports.size(), 2);
    var i = exports.get(0);
    assertEquals(i.getPath(), "hello/world");
    i = exports.get(1);
    assertEquals(i.getPath(), "world/world");
    assertEquals(i.getMode(), Mode.All);
  }

  @ParameterizedTest
  @ValueSource(strings = {"imports"})
  void ensureReadPathSpecificationCanBeReadForMultipleImports(String type) throws IOException {
    val coord =
        readerFor(
            format(
                "service@hello0:world:1.0.0-SNAPSHOT<%s-paths=[just : hello/world, all  :   world/world ]>",
                type));
    val results = scanner.readDependencies(coord);
    assertEquals(results.size(), 1);
    val imports = results.get(0).getImports();
    assertEquals(imports.size(), 2);
    var i = imports.get(0);
    assertEquals(i.getPath(), "hello/world");
    i = imports.get(1);
    assertEquals(i.getPath(), "world/world");
    assertEquals(i.getMode(), Mode.All);
  }

  @Test
  void ensureReadPathSpecificationCanBeReadForMultipleImports_no_whitespace() throws IOException {
    val coord =
        readerFor(
            format(
                "service@hello0:world:1.0.0-SNAPSHOT<imports-paths=[just:hello/world,all:world/world]>"));
    val results = scanner.readDependencies(coord);
    assertEquals(results.size(), 1);
    val imports = results.get(0).getImports();
    assertEquals(imports.size(), 2);
    var i = imports.get(0);
    assertEquals(i.getPath(), "hello/world");
    i = imports.get(1);
    assertEquals(i.getPath(), "world/world");
    assertEquals(i.getMode(), Mode.All);
  }

  @Test
  void ensureReadPathSpecificationCanBeReadForMultipleExports_no_whitespace() throws IOException {
    val coord =
        readerFor(
            format(
                "service@hello0:world:1.0.0-SNAPSHOT<exports-paths=[just:hello/world,all:world/world]>"));
    val results = scanner.readDependencies(coord);
    assertEquals(results.size(), 1);
    val exports = results.get(0).getExports();
    assertEquals(exports.size(), 2);
    var i = exports.get(0);
    assertEquals(i.getPath(), "hello/world");
    i = exports.get(1);
    assertEquals(i.getPath(), "world/world");
    assertEquals(i.getMode(), Mode.All);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "service@io.sunshower.coolbeans:whatever:1.0.0-Final<"
            + "exports-paths=["
            + " just:hello/world, "
            + " all:how/are/you?"
            + "]; "
            + "re-export; "
            + "optional; "
            + "services=import; "
            + "imports-paths=["
            + "just:whatever, "
            + "all:these/are/cool/paths/?]>,"
            + "service@io.sunshower.coolbeans2:whatever:1.0.0-Final<"
            + " exports-paths=["
            + "   just:hello/world, "
            + "   all:how/are/you?]; "
            + "   re-export; "
            + "   optional; "
            + "   services=export; "
            + "   imports-paths=["
            + "       just:whatever, "
            + "       class:io.sunshower.whatever.MyType]"
            + " >"
      })
  void ensureComplexPathSpecsWorkWithClass(String pathSpec) throws IOException {
    val results = scanner.readDependencies(readerFor(pathSpec));
    assertEquals(2, results.size());

    val r = results.get(1);
    assertEquals(r.getImports().get(1).getMode(), Mode.Class);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "service@io.sunshower.coolbeans:whatever:1.0.0-Final<"
            + "exports-paths=["
            + " just:hello/world, "
            + " all:how/are/you?"
            + "]; "
            + "re-export; "
            + "optional; "
            + "services=import; "
            + "imports-paths=["
            + "just:whatever, "
            + "all:these/are/cool/paths/?]>,"
            + "service@io.sunshower.coolbeans2:whatever:1.0.0-Final<"
            + " exports-paths=["
            + "   just:hello/world, "
            + "   all:how/are/you?]; "
            + "   re-export; "
            + "   optional; "
            + "   services=export; "
            + "   imports-paths=["
            + "       just:whatever, "
            + "       all:these/are/cool/paths/*]"
            + " >"
      })
  void ensureComplexPathSpecsWork(String pathSpec) throws IOException {
    val results = scanner.readDependencies(readerFor(pathSpec));
    assertEquals(2, results.size());
  }

  @Test
  void ensureParsingFailingManifestWorks() throws IOException {
    val results =
        scanner.readDependencies(readerFor(" library@io.sunshower:test-plugin-1:1.0.0-SNAPSHOT\n"));
    assertEquals(results.size(), 1);
  }

  @Test
  void ensureParsingFailingManifestWorks2() throws IOException {
    val results =
        scanner.readDependencies(readerFor(" library@io.sunshower:test-plugin-1:1.0.0-SNAPSHOT"));
    assertEquals(results.size(), 1);
  }

  private PushbackReader readerFor(String s) {
    return new PushbackReader(new StringReader(s));
  }
}
