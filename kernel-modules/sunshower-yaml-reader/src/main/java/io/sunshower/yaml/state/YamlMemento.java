package io.sunshower.yaml.state;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.CoordinateSpecification;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.core.SemanticVersion;
import io.zephyr.kernel.memento.Memento;
import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import lombok.val;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "PMD.DataflowAnomalyAnalysis"})
public class YamlMemento implements Memento {

  /** eh. Yamlbeans requires public state. Whatevs. */
  public String name;

  public Map<String, Object> values;

  public Object value;
  public List<YamlMemento> children;

  public YamlMemento(String name) {
    this();
    this.name = name;
  }

  public YamlMemento() {
    this.values = new HashMap<>();
    this.children = new ArrayList<>();
  }

  @Override
  public void write(String name, Object value) {
    values.put(name, String.valueOf(value));
  }

  @Override
  public void write(String name, int item) {
    values.put(name, item);
  }

  @Override
  public void write(String name, long item) {
    values.put(name, name);
  }

  @Override
  public void write(String name, String value) {
    values.put(name, value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Memento child(String name) {
    val child = new YamlMemento(name);
    children.add(child);
    return child;
  }

  @Override
  public Memento childNamed(String name) {
    for (val child : children) {
      if (name.equals(child.name)) {
        return child;
      }
    }
    throw new NoSuchElementException("No child named " + name);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <U> U read(String name, Class<U> value) {
    if (Coordinate.class.isAssignableFrom(value)) {
      return (U) readCoordinate(name);
    }
    if (CoordinateSpecification.class.isAssignableFrom(value)) {
      return (U) readCoordinateSpecification(name);
    }
    return (U) values.get(name);
  }

  private CoordinateSpecification readCoordinateSpecification(String cspecName) {
    val child = childNamed(cspecName);
    val group = child.read("group", String.class);
    val name = child.read("name", String.class);
    val specification = child.read("version", String.class);
    return new CoordinateSpecification(group, name, specification);
  }

  private Coordinate readCoordinate(String cgroupName) {
    val child = childNamed(cgroupName);
    val group = child.read("group", String.class);
    val name = child.read("name", String.class);
    val version = child.read("version", String.class);
    return new ModuleCoordinate(name, group, new SemanticVersion(version));
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void flush() throws IOException {}

  @Override
  public void write(OutputStream outputStream) throws Exception {
    val writer = new YamlWriter(new OutputStreamWriter(outputStream));
    writer.getConfig().setClassTag("memento", Memento.class);
    writer.write(this);
    writer.close();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void read(InputStream inputStream) throws Exception {
    val reader = new YamlReader(new InputStreamReader(inputStream));
    reader.getConfig().setClassTag("memento", Memento.class);
    val result = reader.read(YamlMemento.class);

    this.values = result.values;
    this.value = result.value;
    this.name = result.name;
    this.children = result.children;
  }

  @Override
  public List<Memento> getChildren(String name) {
    return children.stream().filter(t -> t.name.equals(name)).collect(Collectors.toList());
  }

  @Override
  public Path locate(String prefix, FileSystem fs) {
    return fs.getPath(String.format("%s.yaml", prefix));
  }
}
