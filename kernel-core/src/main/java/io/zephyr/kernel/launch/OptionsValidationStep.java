package io.zephyr.kernel.launch;

import io.zephyr.kernel.core.Validatable;
import io.zephyr.kernel.core.ValidationError;
import io.zephyr.kernel.core.ValidationErrors;
import io.zephyr.kernel.launch.validations.AbstractFileValidationStep;
import lombok.val;

public class OptionsValidationStep extends AbstractFileValidationStep {

  private static final long serialVersionUID = -108177900627841768L;

  @Override
  public ValidationErrors validate(Validatable<KernelOptions> validatable, KernelOptions target) {
    val f = target.getHomeDirectory();
    if (f == null) {
      return ValidationErrors.of(new ValidationError());
    }
    return doValidate(target, f.getAbsolutePath());
  }
}
