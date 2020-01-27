package io.zephyr.kernel.ext.el;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mvel2.MVEL;
import org.mvel2.compiler.ExecutableLiteral;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.impl.CachingMapVariableResolverFactory;

class MVELExtensionTest {

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
}
