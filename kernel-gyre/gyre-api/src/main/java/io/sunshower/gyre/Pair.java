package io.sunshower.gyre;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class Pair<K, V> {
  public final K fst;
  public final V snd;

  public static final <K, V> Pair<K, V> of(K k, V v) {
    return new Pair<>(k, v);
  }
}
