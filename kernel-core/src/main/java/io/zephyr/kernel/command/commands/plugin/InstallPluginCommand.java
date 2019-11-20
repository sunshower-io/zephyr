package io.zephyr.kernel.command.commands.plugin;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Console;
import io.zephyr.api.Result;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.command.AbstractCommand;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.PluginEvents;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import lombok.val;
import picocli.CommandLine;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static io.zephyr.kernel.core.PluginEvents.*;

@CommandLine.Command(name = "install")
public class InstallPluginCommand extends AbstractCommand {

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
        PLUGIN_INSTALLATION_INITIATED,
        PLUGIN_INSTALLATION_FAILED,
        PLUGIN_INSTALLATION_COMPLETE,
        PLUGIN_SET_INSTALLATION_COMPLETE,
        PLUGIN_SET_INSTALLATION_INITIATED);

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
      console.errorln("Failed to install plugins.  Reason: {0}", ex.getMessage());
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
        results.add(new URL(normalized));
      } catch (MalformedURLException e) {
        console.errorln("url ''{0}'' isn't valid--will not be installed", normalized);
      }
    }
    return results;
  }

  private String normalize(String url, Console console) {
    if (url.startsWith(".") || url.startsWith("/")) {
      val result = new File(url).getAbsoluteFile().getAbsolutePath();
      val fileResult = String.format("file://%s", result);
      console.successln("Normalizing {0} -> {1}", url, fileResult);
      return fileResult;
    }
    return url;
  }

  private void logUrls(Console console) {
    console.successln("Will install:");
    for (val url : urls) {
      console.successln("\t{0}", url);
    }
  }

  static final class PluginInstallationListener implements EventListener<Object> {
    final Console console;

    PluginInstallationListener(Console console) {
      this.console = console;
    }

    @Override
    public void onEvent(EventType type, Event<Object> event) {

      val pluginEvent = (PluginEvents) type;
      switch (pluginEvent) {
        case PLUGIN_SET_INSTALLATION_INITIATED:
          console.successln("Successfully scheduled plugin installation set");
          break;
        case PLUGIN_INSTALLATION_COMPLETE:
          val module = (Module) event.getTarget();
          console.successln(
              "Successfully installed plugin ''{0}''", module.getCoordinate().toCanonicalForm());
          break;
        case PLUGIN_INSTALLATION_INITIATED:
          console.successln("Beginning download of plugin at ''{0}''", event.getTarget());
          break;
        case PLUGIN_INSTALLATION_FAILED:
          console.errorln("Failed to install plugin ''{0}''", event.getTarget());
          break;
        case PLUGIN_SET_INSTALLATION_COMPLETE:
          console.successln("successfully installed plugins");
          break;
      }
    }
  }
}
