package io.sunshower.kernel.test;

import java.lang.annotation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Test
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = KernelTestConfiguration.class)
@ExtendWith({KernelExtension.class, SpringExtension.class})
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface KernelTest {}
