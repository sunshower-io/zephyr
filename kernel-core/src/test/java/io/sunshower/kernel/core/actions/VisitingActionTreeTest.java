package io.sunshower.kernel.core.actions;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.JUnitUseExpected",
  "PMD.AvoidDuplicateLiterals",
  "PMD.UseProperClassLoader",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage",
})
class VisitingActionTreeTest {

  @Test
  void ensureActionTreeHeightIsCorrectForTreeWithFanout() {
    val root = new VisitingActionTree.ActionNode(null);
    val c1 = new VisitingActionTree.ActionNode(null);
    val c2 = new VisitingActionTree.ActionNode(null);

    val gc11 = new VisitingActionTree.ActionNode(null);
    val gc12 = new VisitingActionTree.ActionNode(null);

    val ggc11 = new VisitingActionTree.ActionNode(null);
    val ggc12 = new VisitingActionTree.ActionNode(null);

    c1.addDependency(gc11);
    c1.addDependency(gc12);

    gc11.addDependency(ggc11);
    gc11.addDependency(ggc12);

    root.addDependency(c1);
    root.addDependency(c2);

    assertEquals(VisitingActionTree.heightOf(root, 0), 4, "must have height 4");
    assertEquals(VisitingActionTree.sizeOf(root, 0), 7, "must have size 7");
  }

  @Test
  void ensureActionTreeHeightIsCorrectForDegenerateTree() {
    val root = new VisitingActionTree.ActionNode(null);
    val c1 = new VisitingActionTree.ActionNode(null);
    val c2 = new VisitingActionTree.ActionNode(null);
    c1.addDependency(c2);
    root.addDependency(c1);

    assertEquals(VisitingActionTree.heightOf(root, 0), 3, "root must have height 3");
    assertEquals(VisitingActionTree.heightOf(c1, 0), 2, "child must have height one less");
  }
}
