package io.sunshower.kernel.launch.validations;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.core.ValidationErrors;
import io.sunshower.kernel.launch.KernelOptions;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.val;

public class SystemEnvironmentVariableValidationStep extends AbstractFileValidationStep {

  static final Logger log =
      Logger.getLogger("SunshowerKernel", "i18n.io.sunshower.kernel.launch.KernelOptions");

  static final String SOURCE = "ENVIRONMENT";

  final Map<String, String> environment;

  public SystemEnvironmentVariableValidationStep(String environmentVariable) {
    this(environmentVariable, System.getenv());
  }

  public SystemEnvironmentVariableValidationStep(
      String environmentVariable, @NonNull Map<String, String> env) {
    super(environmentVariable, SOURCE);
    this.environment = env;
  }

  @Override
  public ValidationErrors validate(Validatable<KernelOptions> validatable, KernelOptions target) {
    if (target.getHomeDirectory() != null) {
      log.log(Level.INFO, "options.homedirectory.exists", target.getHomeDirectory());
      return ValidationErrors.empty();
    }
    val property = environment.get(keyName);
    return doValidate(target, property);
  }
}
