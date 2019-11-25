package io.sunshower.yaml.state;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.test.common.Tests;
import java.io.*;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "PMD.AvoidDuplicateLiterals"})
class YamlMementoTest {

  private File file;
  private YamlMemento<Object> memento;

  @BeforeEach
  void setUp() throws IOException {
    file = new File(Tests.createTemp("yaml-test"), "test.yaml");

    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    memento = new YamlMemento<>("test");
  }

  @AfterEach
  void tearDown() {
    if (file.exists()) {
      file.delete();
    }
  }

  @Test
  void ensureWritingIntegerWorks() throws Exception {
    memento.write("hello", "world");
    try (val output = new FileOutputStream(file)) {
      memento.write(output);
    }

    memento = new YamlMemento<>("test");

    try (val input = new FileInputStream(file)) {
      memento.read(input);
      assertEquals(memento.read("hello", String.class), "world");
    }
  }

  @Test
  void ensureWritingComplexMementoWorks() throws Exception {
    memento.write("hello", "world");

    val child = memento.child("child1", String.class);
    child.write("another", "value");
    val gchild = child.child("coolbeans", String.class);
    gchild.write("supbean", "nupbean");

    try (val output = new FileOutputStream(file)) {
      memento.write(output);
    }

    try (val input = new FileInputStream(file)) {
      memento.read(input);
      assertEquals(memento.read("hello", String.class), "world");
      assertEquals(memento.children.size(), 1);

      val ch = memento.children.get(0);
      assertEquals(ch.read("another", String.class), "value");
    }
  }
}
