package io.zephyr.kernel.launch;

import io.zephyr.common.ChainedValidationStep;
import io.zephyr.kernel.core.ValidationStep;
import io.zephyr.kernel.launch.validations.SystemEnvironmentVariableValidationStep;
import io.zephyr.kernel.launch.validations.SystemPropertyFileValidationStep;
import lombok.val;

public class KernelOptionsValidations {

  public static ValidationStep<KernelOptions> homeDirectory() {
    val result = new ChainedValidationStep<KernelOptions>(new OptionsValidationStep());

    result.add(new SystemPropertyFileValidationStep("sunshower.home"));
    result.add(new SystemEnvironmentVariableValidationStep("SUNSHOWER_HOME"));
    result.add(new SystemPropertyFileValidationStep("user.home"));
    result.add(new SystemPropertyFileValidationStep("user.dir"));

    return result;
  }
}
