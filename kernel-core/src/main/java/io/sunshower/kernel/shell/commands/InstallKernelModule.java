package io.sunshower.kernel.shell.commands;

//import io.sunshower.kernel.concurrency.ConcurrentProcess;
//import io.sunshower.kernel.concurrency.Processor;
//import io.sunshower.kernel.process.ModuleInstallProcess;
import java.util.List;
import picocli.CommandLine;

@CommandLine.Command(name = "kernel-module")
@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.DataflowAnomalyAnalysis"})
public class InstallKernelModule implements Runnable {

  @CommandLine.Parameters private List<String> modules;
  @CommandLine.ParentCommand private InstallCommand commandSet;

  @Override
  public void run() {
    //    val kernel = commandSet.context.getKernel();
    //    val proc = new ModuleInstallProcess(modules.get(0), kernel);
    //    kernel
    //        .getScheduler()
    //        .registerHandler(
    //            new Processor() {
    //              @Override
    //              public String getChannel() {
    //                return ModuleInstallProcess.channel;
    //              }
    //
    //              @Override
    //              public void process(ConcurrentProcess process) {
    //                process.perform();
    //              }
    //            });
    //    kernel.getScheduler().scheduleTask(proc);
  }
}
