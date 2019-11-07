package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;

/**
 * provides a scope suitable for graph reductions. Scopes in graph reductions can be challenging,
 * for instance, consider the following computation graph:
 *
 * <p>task a -> task b -> task c
 *
 * <p>(task a depends on (b, c)), each of which produce a value with the same name and type, how
 * should we resolve this?
 *
 * <p>In this case, we allow both to coexist because we have an arbitrary, yet consistent
 * topological ordering. User interfaces may choose to expose this topological ordering for
 * debugging purposes
 */
@SuppressWarnings({"PMD.NullAssignment", "PMD.DataflowAnomalyAnalysis"})
public final class ReductionScope implements Context, HierarchicalScope {

  private final ReductionScope parent;

  /** the "currently executing" task */
  private final Object enclosingValue;

  private final Context globals;
  private final Map<String, Element> locals;

  private ReductionScope(Context globals) {
    this.parent = null;
    this.enclosingValue = null;
    this.globals = globals;
    this.locals = new HashMap<>(2);
  }

  private ReductionScope(ReductionScope parent, Object enclosingValue) {
    this.globals = null;
    this.parent = parent;
    this.enclosingValue = enclosingValue;
    // using ultra-dense hashmaps /may/ be a mistake, but consider that
    // a: a task typically has very few locals
    // and b: there are a fuck-ton of these in large reductions
    this.locals = new HashMap<>(2);
  }

  public static ReductionScope newRoot(Context context) {
    return new ReductionScope(context);
  }

  public static Context newContext() {
    return new ReductionScope(null, null);
  }

  @Override
  public Object set(String key, Object value) {
    val el = locals.get(key);
    if (el == null) {
      locals.put(key, new Element(key, value));
      return true;
    }
    val result = el.getValue();
    el.setValue(value);
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    val result = resolve(key);
    if (result != null) {
      return (T) result.getValue();
    }
    return null;
  }

  @Override
  @SuppressFBWarnings
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {

    var c = this;
    for (; ; ) {
      for (val local : c.locals.values()) {
        val el = local.getValue();
        if (el != null && type.isAssignableFrom(el.getClass())) {
          return (T) el;
        }
      }

      if (c.parent == null) {
        for (val local : c.globals.getLocals()) {
          val el = local.getValue();
          if (el != null && type.isAssignableFrom(el.getClass())) {
            return (T) el;
          }
        }
        return null;
      }
      c = c.parent;
    }
  }

  @Override
  public Scope getRootScope() {
    var c = this;
    for (; ; ) {
      if (c.parent == null) {
        return c;
      }
      c = c.parent;
    }
  }

  @Override
  public Object getEnclosingValue() {
    return enclosingValue;
  }

  @Override
  public List<Element> getLocals() {
    return new ArrayList<>(locals.values());
  }

  @Override
  public Element define(String name, Object value) {
    var el = locals.get(name);
    if (el == null) {
      el = new Element(name, value);
      locals.put(name, el);
    }
    return el;
  }

  @Override
  public Element delete(String name, boolean localOnly) {

    if (localOnly) {
      return locals.remove(name);
    }

    var c = this;
    for (; ; ) {
      if (c.locals.containsKey(name)) {
        return c.locals.remove(name);
      }
      if (c.parent == null) {
        break;
      }
      c = c.parent;
    }
    return null;
  }

  @Override
  @SuppressFBWarnings
  public Element resolve(String name, boolean localOnly) {
    if (localOnly) {
      return locals.get(name);
    }

    var c = this;
    for (; ; ) {
      if (c.locals.containsKey(name)) {
        return c.locals.get(name);
      }
      if (c.parent == null) {
        if (c.globals == null) {
          return null;
        }
        val result = c.globals.get(name);
        if (result != null) {
          return new Element(name, result);
        }
      }
      c = c.parent;
    }
  }

  @Override
  public Object resolveValue(String name, boolean localOnly) {
    val el = resolve(name, localOnly);
    if (el != null) {
      return el.getValue();
    }
    return null;
  }

  @Override
  public Scope getEnclosingScope() {
    return parent;
  }

  @Override
  public ReductionScope pushScope(Object enclosingValue) {
    return new ReductionScope(this, enclosingValue);
  }

  @Override
  public Scope popScope() {
    return parent;
  }
}
