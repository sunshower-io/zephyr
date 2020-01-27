package io.zephyr.kernel.ext.el;

import io.zephyr.Context;
import io.zephyr.api.Query;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.mvel2.MVEL;

public final class ExecutableStatementPredicate<T> implements Predicate<T> {

  private final Query<T> query;
  private volatile Boolean cached;

  public ExecutableStatementPredicate(Query<T> query) {
    this.query = query;
  }

  @Override
  public boolean test(T t) {

    Boolean local = cached;

    if (local == null) {
      synchronized (this) {
        local = cached;
        if (local == null) {
          cached = local = evaluate(t);
        }
      }
    }
    return local;
  }

  private Boolean evaluate(T t) {
    return MVEL.evalToBoolean(query.getQuery(), new PredicateContext<>(t, query.getContext()));
  }

  @AllArgsConstructor
  public static final class PredicateContext<T> {
    public final T value;
    public final Context<T> context;
  }
}
