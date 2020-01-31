package io.zephyr.kernel.ext.el;

import io.zephyr.api.Query;
import io.zephyr.kernel.extensions.ExpressionLanguageExtension;
import java.util.function.Predicate;

public class MVELExtension implements ExpressionLanguageExtension {
  static final String LANGUAGE = "mvel";

  @Override
  public <T> boolean supports(Query<T> query) {
    return LANGUAGE.equalsIgnoreCase(query.getLanguage().trim());
  }

  @Override
  public <T> Predicate<T> createPredicate(Query<T> query) {

    return new ExecutableStatementPredicate(query);
  }
}
