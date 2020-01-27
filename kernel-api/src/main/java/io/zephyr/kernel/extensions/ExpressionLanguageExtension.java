package io.zephyr.kernel.extensions;

import io.zephyr.api.Query;

import java.util.function.Predicate;

public interface ExpressionLanguageExtension {

  <T> boolean supports(Query<T> query);

  <T> Predicate<T> createPredicate(Query<T> query);
}
