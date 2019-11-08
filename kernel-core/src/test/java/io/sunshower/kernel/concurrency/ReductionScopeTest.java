package io.sunshower.kernel.concurrency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ReductionScopeTest {

  private ReductionScope scope;

  private Context context;

  @BeforeEach
  void setUp() {
    scope = ReductionScope.newRoot(context = ReductionScope.newContext());
  }

  @Test
  void ensureLocatingByClassWorks() {
    val test = mock(ReductionScopeTest.class);
    context.set("Hello", test);
    assertEquals(context.get(ReductionScopeTest.class), test, "must be retrievable");
  }

  @Test
  void ensureLocatingByClassInHierarchyWorks() {

    val test = mock(ReductionScopeTest.class);
    context.set("Hello", test);

    val descendent = scope.pushScope(null).pushScope(null).pushScope(null);
    assertEquals(
        descendent.get(ReductionScopeTest.class), test, "must be retrievable in hierarchy");
  }

  @Test
  void ensureLocatingGlobalValueWorksForDescendantLocals() {
    context.set("test", "whatever");
    val child = scope.pushScope(null);
    val gchild = child.pushScope(null);

    assertNull(gchild.resolveValue("test", true), "global value must not appear in scope locals");
  }

  @Test
  void ensureLocatingGlobalValueWorksForDescendant() {
    context.set("test", "whatever");
    val child = scope.pushScope(null);
    val gchild = child.pushScope(null);

    assertEquals(gchild.resolveValue("test"), "whatever", "global value must be correct");
  }

  @Test
  void ensureSearchingLocallyWorks() {
    scope.define("test", "sup");
    assertEquals(scope.resolveValue("test"), "sup", "value must be correct");
  }

  @Test
  void ensureSearchingInHierarchyWorks() {
    val nscope = scope.pushScope(null);
    scope.define("test", "sup");
    assertEquals(nscope.resolveValue("test"), "sup", "value from parent scope must be correct");
  }

  @Test
  void ensureLocatingRootWorks() {
    val nscope = scope.pushScope(null);
    assertEquals(nscope.getEnclosingScope(), scope, "enclosing scope must be correct");
  }

  @Test
  void ensureDescendantProducesCorrectScope() {
    val cscope = scope.pushScope(null);
    val gcscope = cscope.pushScope(null);
    assertEquals(gcscope.getRootScope(), scope, "root scope must be correct");
  }

  @Test
  void ensureParentScopeIsCorrectInHierarchy() {
    val cscope = scope.pushScope(null);
    val gcscope = cscope.pushScope(null);
    assertEquals(gcscope.getEnclosingScope(), cscope, "root scope must be correct");
  }
}
