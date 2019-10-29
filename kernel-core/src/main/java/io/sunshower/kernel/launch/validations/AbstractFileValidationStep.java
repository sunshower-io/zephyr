package io.sunshower.kernel.launch.validations;

import io.sunshower.common.io.FilePermissionChecker;
import io.sunshower.kernel.ObjectCheckException;
import io.sunshower.kernel.core.ValidationError;
import io.sunshower.kernel.core.ValidationErrors;
import io.sunshower.kernel.core.ValidationStep;
import io.sunshower.kernel.launch.KernelOptions;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.val;

public abstract class AbstractFileValidationStep implements ValidationStep<KernelOptions> {

  protected static final Logger log =
      Logger.getLogger("SunshowerKernel", "i18n.io.sunshower.kernel.launch.KernelOptions");

  protected final String source;
  protected final String keyName;

  protected AbstractFileValidationStep(
      @NonNull final String keyName, @NonNull final String source) {
    this.keyName = keyName;
    this.source = source;
  }

  protected ValidationErrors doValidate(KernelOptions target, String property) {
    log.log(Level.FINE, "options.file.locating", new Object[] {keyName, source});
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
        return ValidationErrors.of(new ValidationError(file, ex));
      }
    }
    return ValidationErrors.empty();
  }
}
