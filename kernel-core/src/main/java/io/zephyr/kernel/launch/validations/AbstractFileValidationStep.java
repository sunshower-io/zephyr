package io.zephyr.kernel.launch.validations;

import io.zephyr.common.io.FilePermissionChecker;
import io.zephyr.kernel.ObjectCheckException;
import io.zephyr.kernel.core.KernelException;
import io.zephyr.kernel.core.ValidationError;
import io.zephyr.kernel.core.ValidationErrors;
import io.zephyr.kernel.core.ValidationStep;
import io.zephyr.kernel.launch.KernelOptions;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.val;

public abstract class AbstractFileValidationStep implements ValidationStep<KernelOptions> {

  protected static final Logger log =
      Logger.getLogger("SunshowerKernel", "i18n.io.sunshower.kernel.launch.KernelOptions");
  private static final long serialVersionUID = -3634337260413171690L;

  protected final String source;
  protected final String keyName;

  protected AbstractFileValidationStep(
      @NonNull final String keyName, @NonNull final String source) {
    this.keyName = keyName;
    this.source = source;
  }

  protected AbstractFileValidationStep() {
    this.keyName = "";
    this.source = "";
  }

  protected ValidationErrors doValidate(KernelOptions target, String property) {
    log.log(Level.FINE, "options.file.locating", new Object[] {keyName, source});
    if (property != null) {
      val file = new File(property).getAbsoluteFile();
      checkParent(file);
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

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void checkParent(File file) {
    if (!file.exists()) {
      if (!file.mkdirs()) {
        throw new KernelException("Failed to create file: " + file);
      }
    }
  }
}
