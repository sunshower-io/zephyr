package io.sunshower.gyre;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public interface Scope {

  Scope getEnclosingScope();

  Scope getRootScope();

  Object getEnclosingValue();

  List<Element> getLocals();

  /**
   * @param name the name to add
   * @param value the value to add
   * @return the corresponding element
   */
  Element define(String name, Object value);

  /**
   * Remove an element from this scope. Care should be taken calling this on the scope hierarchy
   * (i.e. localOnly = true) as that can have unintended consequences for the computation
   *
   * @param name
   * @param localOnly
   * @return the element deleted
   */
  Element delete(String name, boolean localOnly);

  default Element delete(String name) {
    return delete(name, true);
  }

  /**
   * the element looks at the current scope first before traversing to the root
   *
   * @param name the name to look up
   * @return the element, if it exists in this reduction topology
   */
  default Element resolve(String name) {
    return resolve(name, false);
  }

  /**
   * @param name the name to resolve
   * @param localOnly do we traverse the scope hierarchy for a value?
   * @return
   */
  Element resolve(String name, boolean localOnly);

  /**
   * @param name the name to lookup
   * @param localOnly the value. This can be ambiguous as a value could be null or undefined.
   * @return the value, null or otherwise
   */
  Object resolveValue(String name, boolean localOnly);

  default Object resolveValue(String name) {
    return resolveValue(name, false);
  }

  class Element {

    /** The value of this element */
    @Getter @Setter private Object value;

    /** Computations are permitted to alter an element's value (e.g. i++), but not its name */
    private final String name;

    public Element(String name, Object value) {
      this.name = name;
      this.value = value;
    }
  }
}
