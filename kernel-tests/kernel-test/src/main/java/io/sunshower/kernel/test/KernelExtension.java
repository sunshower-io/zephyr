package io.sunshower.kernel.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class KernelExtension extends SpringExtension {
  public KernelExtension() {
    super();
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    super.beforeAll(context);
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    super.afterAll(context);
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context)
      throws Exception {
    super.postProcessTestInstance(testInstance, context);
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    super.beforeEach(context);
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) throws Exception {
    super.beforeTestExecution(context);
  }

  @Override
  public void afterTestExecution(ExtensionContext context) throws Exception {
    super.afterTestExecution(context);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    super.afterEach(context);
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    return super.supportsParameter(parameterContext, extensionContext);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    return super.resolveParameter(parameterContext, extensionContext);
  }
}
