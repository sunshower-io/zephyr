package io.zephyr.kernel.launch.validations;

import static io.zephyr.kernel.core.ValidationErrors.empty;

import io.zephyr.kernel.core.Validatable;
import io.zephyr.kernel.core.ValidationErrors;
import io.zephyr.kernel.launch.KernelOptions;
import java.util.logging.Level;

public class SystemPropertyFileValidationStep extends AbstractFileValidationStep {

  static final String SOURCE = "SystemProperties";
  private static final long serialVersionUID = 1867557287065178312L;

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
