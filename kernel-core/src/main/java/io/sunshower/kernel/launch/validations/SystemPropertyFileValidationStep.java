package io.sunshower.kernel.launch.validations;

import static io.sunshower.kernel.core.ValidationErrors.empty;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.core.ValidationErrors;
import io.sunshower.kernel.launch.KernelOptions;
import java.util.logging.Level;

public class SystemPropertyFileValidationStep extends AbstractFileValidationStep {

  static final String SOURCE = "SystemProperties";

  public SystemPropertyFileValidationStep(String propertyName) {
    super(propertyName, SOURCE);
  }

  /**
   * prereq target.homeDirectory == null
   *
   * @param validatable
   * @param target
   * @return
   */
  @Override
  public ValidationErrors validate(Validatable<KernelOptions> validatable, KernelOptions target) {
    if (target.getHomeDirectory() != null) {
      log.log(Level.INFO, "options.homedirectory.exists", target.getHomeDirectory());
      return empty();
    }
    return doValidate(target, System.getProperty(keyName));
  }
}
