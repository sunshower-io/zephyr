package io.sunshower.kernel.test;

import java.lang.annotation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Test
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(KernelExtension.class)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface KernelTest {
  Class<?> value();
}
