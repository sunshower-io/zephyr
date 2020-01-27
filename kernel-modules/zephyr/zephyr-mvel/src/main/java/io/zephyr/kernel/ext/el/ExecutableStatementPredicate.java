package io.zephyr.kernel.ext.el;

import io.zephyr.api.Query;
import java.util.function.Predicate;
import org.mvel2.MVEL;

final class ExecutableStatementPredicate<T> implements Predicate<T> {

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
          cached = local = evaluate();
        }
      }
    }
    return local;
  }

  private Boolean evaluate() {
    return MVEL.evalToBoolean(query.getQuery(), query.getContext());
  }
}
