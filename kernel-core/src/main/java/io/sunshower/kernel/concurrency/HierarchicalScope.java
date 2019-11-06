package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.Scope;

public interface HierarchicalScope extends Scope {

  Scope pushScope(Object enclosingObject);

  Scope popScope();
}
