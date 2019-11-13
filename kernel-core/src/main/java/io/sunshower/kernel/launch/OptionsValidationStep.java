package io.sunshower.kernel.launch;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.core.ValidationErrors;
import io.sunshower.kernel.launch.validations.AbstractFileValidationStep;
import lombok.val;

public class OptionsValidationStep extends AbstractFileValidationStep {

  @Override
  public ValidationErrors validate(Validatable<KernelOptions> validatable, KernelOptions target) {
    val f = target.getHomeDirectory();
    return doValidate(target, f.getAbsolutePath());
  }
}
