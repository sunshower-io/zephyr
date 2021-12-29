package io.zephyr.breeze;

import io.sunshower.gyre.AbstractDirectedGraph;
import io.sunshower.gyre.Pair;
import io.sunshower.gyre.Scope;
import io.zephyr.kernel.concurrency.DefaultProcess;
import io.zephyr.kernel.concurrency.Process;
import io.zephyr.kernel.concurrency.Task;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.val;

@Log
public final class Breeze {

  private Breeze() {

  }


  @NonNull
  static Process<Label> newTaskGraph(@NonNull Class<?>... types) {
    val processDefinition = createReductionDefinition(types);
    val result = new AbstractDirectedGraph<Label, Task>();

    for (val type : types) {
      define(type, result);
    }
    val reduction = processDefinition.reduction;

    return new DefaultProcess<Label>(
        processDefinition.displayName,
        reduction.coalesce(),
        reduction.parallel(), Scope.root(), result);
  }

  private static ReductionDefinition createReductionDefinition(Class<?>[] types) {
    val rd = resolveProcessType(types);
    return new ReductionDefinition(getDisplayName(rd), rd.fst, rd.snd);
  }

  private static String getDisplayName(Pair<Reduction, Class<?>> reduction) {
    if (Constants.DEFAULT_VALUE.equals(reduction.fst.displayName())) {
      return "Reduction[" + reduction.snd.getName() + "]";
    }
    return reduction.fst.displayName();
  }

  private static Pair<Reduction, Class<?>> resolveProcessType(Class<?>[] types) {
    for (val type : types) {
      if (type.isAnnotationPresent(Reduction.class)) {
        return Pair.of(type.getAnnotation(Reduction.class), type);
      }
    }
    throw new IllegalArgumentException(
        "Expected at least one type to be annotated with @Reduction");
  }

  private static void define(Class<?> type, AbstractDirectedGraph<Label, Task> result) {

  }

  @Data
  private static final class ReductionDefinition {

    final String displayName;
    final Reduction reduction;
    final Class<?> reductionClass;
  }
}
