package io.zephyr.kernel;

import io.zephyr.kernel.core.Validatable;

import java.io.Serializable;

public interface Options<T extends Options<T>> extends Serializable, Validatable<T> {


}
