package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.Pair;

public interface Parameters {

  Parameters define(Pair<String, Class<?>> key, Object value);

  Object get(String key);

  Object get(Class<?> value);

  Object get(String key, Class<?> value);
}
