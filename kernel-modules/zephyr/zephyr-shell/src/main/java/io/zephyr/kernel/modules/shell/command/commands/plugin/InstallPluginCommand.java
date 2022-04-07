package io.zephyr.kernel.modules.shell.command.commands.plugin;

import static io.zephyr.api.ModuleEvents.*;
import static io.zephyr.kernel.core.actions.ModulePhaseEvents.*;

import io.sunshower.lang.events.Event;
import io.sunshower.lang.events.EventListener;
import io.sunshower.lang.events.EventType;
import io.zephyr.api.ModuleEvents;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.actions.ModulePhaseEvents;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import io.zephyr.kernel.modules.shell.command.AbstractCommand;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Result;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import picocli.CommandLine;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.UnusedPrivateMethod"})
@CommandLine.Command(name = "install")
public class InstallPluginCommand extends AbstractCommand {

  private static final long serialVersionUID = -8662903213651849534L;
  @CommandLine.Parameters private String[] urls;

  public InstallPluginCommand() {
    super("install");
  }

  @Override
  public Result execute(CommandContext context) {
    val console = context.getService(Console.class);
    if (urls == null || urls.length == 0) {
      console.errorln("No plugin URLs provided.  Not doing anything");
      return Result.failure();
    }

    val kernel = context.getService(Kernel.class);
    if (kernel == null) {
      console.errorln("Kernel is not running (have you run <kernel start>?");
      return Result.failure();
    }
    val listener = new PluginInstallationListener(console);
    kernel.addEventListener(
        listener,
        INSTALLING,
        INSTALL_FAILED,
        INSTALLED,
        MODULE_SET_INSTALLATION_COMPLETED,
        MODULE_SET_INSTALLATION_INITIATED);

    try {
      logUrls(console);
      val actualUrls = normalize(console);
      val request = new ModuleInstallationGroup();
      for (val url : actualUrls) {
        val installationReq = new ModuleInstallationRequest();
        installationReq.setLocation(url);
        request.add(installationReq);
      }

      kernel.getModuleManager().prepare(request).commit().toCompletableFuture().get();
    } catch (Exception ex) {
      console.errorln("Failed to install plugins.  Reason: %s", ex.getMessage());
    } finally {
      kernel.removeEventListener(listener);
    }
    return Result.success();
  }

  private List<URL> normalize(Console console) {
    val results = new ArrayList<URL>(urls.length);
    for (val url : urls) {
      String normalized = normalize(url, console);
      try {
        results.add(Paths.get(url).toUri().toURL());
      } catch (MalformedURLException e) {
        console.errorln("url '%s' isn't valid--will not be installed", normalized);
      }
    }
    return results;
  }

  private String normalize(String url, Console console) {
    if (url.charAt(0) == '.' || url.charAt(0) == '/') {
      val result = new File(url).getAbsoluteFile().getAbsolutePath();
      val fileResult = String.format("file://%s", result);
      console.successln("Normalizing %s -> %s", url, fileResult);
      return fileResult;
    }
    return url;
  }

  private void logUrls(Console console) {
    console.successln("Will install:");
    for (val url : urls) {
      console.successln("\t%s", url);
    }
  }

  @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault")
  static final class PluginInstallationListener implements EventListener<Object> {
    final Console console;

    PluginInstallationListener(Console console) {
      this.console = console;
    }

    @Override
    public void onEvent(EventType type, Event<Object> event) {

      if (type instanceof ModuleEvents) {
        val pluginEvent = (ModuleEvents) type;
        switch (pluginEvent) {
          case INSTALLED:
            val module = (Module) event.getTarget();
            console.successln(
                "Successfully installed plugin '%s'", module.getCoordinate().toCanonicalForm());
            break;
          case INSTALLING:
            console.successln("Beginning download of plugin at '%s'", event.getTarget());
            break;
          case INSTALL_FAILED:
            console.errorln("Failed to install plugin '%s'", event.getTarget());
            break;
        }

      } else {
        val pluginEvent = (ModulePhaseEvents) type;
        switch (pluginEvent) {
          case MODULE_SET_INSTALLATION_COMPLETED:
            console.successln("successfully installed plugins");
            break;
          case MODULE_SET_INSTALLATION_INITIATED:
            console.successln("Successfully scheduled plugin installation set");
            break;
        }
      }
    }
  }
}
