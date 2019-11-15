package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.launch.KernelLauncher;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationRequest;
import io.sunshower.kernel.shell.Command;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "install")
@SuppressWarnings("PMD.DoNotUseThreads")
public class PluginInstallCommand extends Command {

  @CommandLine.Parameters(description = "urls")
  private String[] urls;

  @Override
  protected int execute() {
    if (kernel == null) {
      KernelLauncher.getConsole().format("Kernel has not been started");
      return 0;
    }
    System.out.println("Installing URLs: " + Arrays.toString(urls));

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
      System.out.println("Error installing plugins: " + e.getMessage());
    }
    return 0;
  }

  private URL getUrl(String url) throws MalformedURLException {
    if (url.startsWith(".") || url.startsWith("/")) {
      return new File(url).getAbsoluteFile().toURI().toURL();
    }
    return new URL(url);
  }
}
