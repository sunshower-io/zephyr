package io.zephyr.kernel.shell.commands;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.PluginEvents;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import io.zephyr.kernel.shell.Color;
import io.zephyr.kernel.shell.Command;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "install")
@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.SystemPrintln",
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.UnusedPrivateMethod"
})
public class PluginInstallCommand extends Command {

  @CommandLine.Parameters(description = "urls")
  private String[] urls;

  public PluginInstallCommand() {
    super(PluginEvents.class);
  }

  @Override
  public void onEvent(EventType type, Event<Object> event) {
    PluginEvents e = (PluginEvents) type;
    switch (e) {
      case PLUGIN_INSTALLATION_FAILED:
        {
          console.writeln("failed to install bundle", Color.colors(Color.Red));
          break;
        }
      case PLUGIN_INSTALLATION_INITIATED:
        {
          console.writeln(
              "attempting to install plugin from %s ",
              Color.colors(Color.Green), event.getTarget());
          break;
        }
      case PLUGIN_INSTALLATION_COMPLETE:
        {
          val module = (Module) event.getTarget();
          console.writeln(
              "successfully installed %s ", Color.colors(Color.Green), module.getCoordinate());
          break;
        }
      case PLUGIN_SET_INSTALLATION_COMPLETE:
        {
          console.writeln("successfully installed plugins ", Color.colors(Color.Green));
          break;
        }
      case PLUGIN_SET_INSTALLATION_INITIATED:
        console.writeln("beginning plugin set installation", Color.colors(Color.Green));
        break;
    }
  }

  @Override
  protected int execute() {
    val installationRequest = new ModuleInstallationGroup();
    for (val url : urls) {
      try {
        val u = getUrl(url);
        val request = new ModuleInstallationRequest();
        request.setLocation(u);
        installationRequest.add(request);
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }
    }
    try {
      kernel.getModuleManager().prepare(installationRequest).commit().toCompletableFuture().get();
    } catch (Exception e) {
      console.writeln("Error installing plugins: %s", Color.colors(Color.Red), e.getMessage());
    }
    return 0;
  }

  private URL getUrl(String url) throws MalformedURLException {
    if (url.charAt(0) == '.' || url.charAt(0) == '/') {
      return new File(url).getAbsoluteFile().toURI().toURL();
    }
    return new URL(url);
  }
}
