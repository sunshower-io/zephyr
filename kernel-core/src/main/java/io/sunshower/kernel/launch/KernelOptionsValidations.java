package io.sunshower.kernel.launch;

import io.sunshower.common.ChainedValidationStep;
import io.sunshower.kernel.core.ValidationStep;
import io.sunshower.kernel.launch.validations.SystemEnvironmentVariableValidationStep;
import io.sunshower.kernel.launch.validations.SystemPropertyFileValidationStep;
import lombok.val;

public class KernelOptionsValidations {

  public static ValidationStep<KernelOptions> homeDirectory() {
    val result = new ChainedValidationStep<KernelOptions>(new OptionsValidationStep());

    result.add(new SystemPropertyFileValidationStep("sunshower.home"));
    result.add(new SystemEnvironmentVariableValidationStep("SUNSHOWER_HOME"));

    return result;
  }
}
