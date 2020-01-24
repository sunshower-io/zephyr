package io.zephyr.kernel.misc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface SuppressFBWarnings {
  String[] value() default {};

  /** Optional documentation of the reason why the warning is suppressed */
  String justification() default "";
}
