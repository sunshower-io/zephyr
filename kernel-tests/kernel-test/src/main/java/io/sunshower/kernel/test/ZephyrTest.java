package io.sunshower.kernel.test;

import java.lang.annotation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

@Test
@Documented
@Retention(RetentionPolicy.RUNTIME)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = KernelTestConfiguration.class)
@ExtendWith({KernelExtension.class, MockitoExtension.class})
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface ZephyrTest {}
