package io.sunshower.kernel.concurrency;

import static java.lang.String.format;

import io.sunshower.gyre.Pair;
import io.sunshower.gyre.Scope;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.val;

public abstract class Task {

  final String name;
  final Parameters parameters;

  protected Task(String name) {
    this.name = name;
    parameters = new TParams();
  }

  public final String getName() {
    return name;
  }

  public final Parameters parameters() {
    return parameters;
  }

  public abstract TaskValue run(Scope scope);

  @AllArgsConstructor
  public static final class TaskValue {
    final Object value;
    final String name;
  }

  public String toString() {
    return format("Task[%s]", name);
  }
}

final class TParams implements Parameters {

  final Map<Pair<String, Class<?>>, Object> values;

  TParams() {
    values = new HashMap<>();
  }

  @Override
  public Parameters define(Pair<String, Class<?>> key, Object value) {
    values.put(key, value);
    return this;
  }

  @Override
  public Object get(String key) {
    for (val s : values.entrySet()) {
      if (key.equals(s.getKey().fst)) {
        return s.getValue();
      }
    }
    return null;
  }

  @Override
  public Object get(Class<?> value) {

    for (val s : values.entrySet()) {
      if (value.equals(s.getKey().snd)) {
        return s.getValue();
      }
    }
    return null;
  }

  @Override
  public Object get(String key, Class<?> value) {
    return values.get(Pair.of(key, value));
  }
}
