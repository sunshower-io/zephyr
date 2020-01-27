package io.zephyr.kernel.ext.el;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import io.zephyr.Context;
import io.zephyr.api.Query;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.extensions.ExpressionLanguageExtension;
import java.util.*;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mvel2.MVEL;
import org.mvel2.compiler.ExecutableLiteral;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.impl.CachingMapVariableResolverFactory;

@ExtendWith(MockitoExtension.class)
class MVELExtensionTest {
  @Mock private Module module;
  @Mock private Coordinate coordinate;

  @Test
  void checkSimpleExpression() {
    ExecutableLiteral simpleResult = (ExecutableLiteral) MVEL.compileExpression("'a' == 'a'");
    assertTrue((Boolean) simpleResult.getLiteral(), "value must be true");
  }

  public static class Holder {

    private final Map<String, Object> context = new HashMap<>();

    public Object get(String key) {
      return context.get(key);
    }
  }

  @Test
  void checkContextExpression() {
    val simpleResult = (ExecutableStatement) MVEL.compileExpression("get('hello')  == 'world'");
    val h = new Holder();
    h.context.put("hello", "world");
    val factory = new CachingMapVariableResolverFactory(Collections.emptyMap());
    val result = simpleResult.getValue(h, factory);
    assertTrue((Boolean) result, "value must be true");
  }

  @Test
  void checkEvalToBoolean() {
    val holder = new Holder();
    holder.context.put("hello", "world");
    val result = MVEL.evalToBoolean("get('hello') == 'world'", holder);
    assertTrue(result, "must be true");
  }

  @Test
  void ensurePredicateWorksForSuccessfulModuleCase() {
    val el = resolve();
    val ctx = new Context<>(module, null);
    given(coordinate.getName()).willReturn("test");
    given(module.getCoordinate()).willReturn(coordinate);
    val query = new Query<>("context.module.coordinate.name == 'test'", "mvel", ctx);
    assertTrue(el.createPredicate(query).test(module), "module name must be test");
  }

  @Test
  void ensurePredicateWorksForUnsuccessfulModuleCase() {
    val el = resolve();
    val ctx = new Context<>(module, null);
    given(coordinate.getName()).willReturn("frapper");
    val query = new Query<>("value.name == 'test'", "mvel", ctx);
    assertFalse(el.createPredicate(query).test(coordinate), "module name must not be test");
  }

  private ExpressionLanguageExtension resolve() {
    return ServiceLoader.load(ExpressionLanguageExtension.class)
        .findFirst()
        .orElseThrow(NoSuchElementException::new);
  }
}
