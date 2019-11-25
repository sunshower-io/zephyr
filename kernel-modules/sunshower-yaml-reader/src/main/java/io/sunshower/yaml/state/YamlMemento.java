package io.sunshower.yaml.state;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import io.zephyr.kernel.memento.Memento;
import java.io.*;
import java.util.*;
import lombok.val;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class YamlMemento<T> implements Memento<T> {

  /** eh. Yamlbeans requires public state. Whatevs. */
  public String name;

  public Map<String, Object> values;

  public Object value;
  public List<YamlMemento<?>> children;

  public YamlMemento(String name) {
    this.name = name;
    this.values = new HashMap<>();
    this.children = new ArrayList<>();
  }

  public YamlMemento() {}

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
  public <U> Memento<U> child(String name, Class<U> type) {
    val child = new YamlMemento<>(name);
    children.add(child);
    return (Memento<U>) child;
  }

  @Override
  public <U> U read(String name, Class<U> value) {
    return (U) values.get(name);
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
}
