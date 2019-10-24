package io.sunshower.kernel.launch;

import io.sunshower.common.ChainedValidationStep;
import io.sunshower.kernel.core.ValidationStep;
import io.sunshower.kernel.launch.validations.SystemPropertyFileValidationStep;
import lombok.val;

public class KernelOptionsValidations {

  public static ValidationStep<KernelOptions> homeDirectory() {
    val result =
        new ChainedValidationStep<KernelOptions>(
            new SystemPropertyFileValidationStep("sunshower.home"));
    return result;
  }
}
