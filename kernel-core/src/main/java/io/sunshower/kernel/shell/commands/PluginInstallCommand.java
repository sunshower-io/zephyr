package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.launch.KernelLauncher;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationRequest;
import lombok.val;
import picocli.CommandLine;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@CommandLine.Command(name = "install")
@SuppressWarnings("PMD.DoNotUseThreads")
public class PluginInstallCommand implements Runnable {

  @CommandLine.Parameters(description = "urls")
  private String[] urls;

  @Override
  public void run() {
    val kernel = KernelLauncher.getKernel();
    if (kernel == null) {
      KernelLauncher.getConsole().format("Kernel has not been started");
      return;
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
  }

  private URL getUrl(String url) throws MalformedURLException {
    if (url.startsWith(".") || url.startsWith("/")) {
      return new File(url).getAbsoluteFile().toURI().toURL();
    }
    return new URL(url);
  }
}
