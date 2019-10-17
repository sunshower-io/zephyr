package io.sunshower.kernel.launch.validations;

import io.sunshower.common.io.FilePermissionChecker;
import io.sunshower.kernel.ObjectCheckException;
import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.core.ValidationError;
import io.sunshower.kernel.core.ValidationStep;
import io.sunshower.kernel.launch.KernelOptions;
import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class SystemPropertyFileValidationStep implements ValidationStep<KernelOptions> {

  static final Logger log =
      Logger.getLogger("SunshowerKernel", "i18n.io.sunshower.kernel.launch.KernelOptions");

  final String propertyName;

  public SystemPropertyFileValidationStep(String propertyName) {
    this.propertyName = propertyName;
  }

  /**
   * prereq target.homeDirectory == null
   *
   * @param validatable
   * @param target
   * @return
   */
  @Override
  public Optional<ValidationError> validate(
      Validatable<KernelOptions> validatable, KernelOptions target) {
    if (target.getHomeDirectory() != null) {
      log.log(Level.INFO, "options.homedirectory.exists", target.getHomeDirectory());
      return Optional.empty();
    }

    val property = System.getProperty(propertyName);
    log.log(Level.FINE, "options.file.locating", new Object[] {propertyName, "SystemProperties"});
    if (property != null) {
      val file = new File(property).getAbsoluteFile();
      val checker =
          new FilePermissionChecker(
              FilePermissionChecker.Type.READ,
              FilePermissionChecker.Type.WRITE,
              FilePermissionChecker.Type.DELETE,
              FilePermissionChecker.Type.EXECUTE);
      try {
        checker.check(file);
        target.setHomeDirectory(file);
        log.log(Level.FINE, "options.file.located", file);
      } catch (ObjectCheckException ex) {
        return Optional.of(new ValidationError(file, ex));
      }
    }

    return Optional.empty();
  }
}
