package io.sunshower.kernel.module;

public class ModuleEntryWrite {

  //  public static final String channel = "kernel:module:module:write";
  //
  //  final Path file;
  //  final Module module;
  //
  //  public ModuleEntryWrite(final Path file, final Module module) {
  //    this.file = file;
  //    this.module = module;
  //  }
  //
  //  @Override
  //  public String getChannel() {
  //    return channel;
  //  }
  //
  //  @Override
  //  @SuppressFBWarnings
  //  public void perform() {
  //    try (val outputStream =
  //        new BufferedWriter(
  //            new OutputStreamWriter(
  //                Files.newOutputStream(
  //                    file,
  //                    StandardOpenOption.WRITE,
  //                    StandardOpenOption.DSYNC,
  //                    StandardOpenOption.APPEND)))) {
  //
  //      val coord = module.getCoordinate();
  //      val entry =
  //          new KernelModuleEntry(
  //              module.getOrder(),
  //              coord.getName(),
  //              coord.getGroup(),
  //              coord.getVersion().toString(),
  //              libraryFiles(module.getLibraries()));
  //      outputStream.write(entry.toString());
  //      outputStream.write("\n");
  //    } catch (IOException ex) {
  //      throw new KernelException(ex);
  //    }
  //  }
  //
  //  private List<String> libraryFiles(Collection<Library> libraries) {
  //    List<String> result = new ArrayList<>(libraries.size());
  //    for (val library : libraries) {
  //      result.add(library.getFile().getAbsolutePath());
  //    }
  //    result.add(module.getAssembly().getFile().getAbsolutePath());
  //    return result;
  //  }
  //
  //  @Override
  //  public int hashCode() {
  //    return channel.hashCode();
  //  }
  //
  //  @Override
  //  public boolean equals(Object o) {
  //    if (o == null) return false;
  //    if (o == this) return true;
  //    if (getClass().equals(o.getClass())) {
  //      return ((ModuleEntryWrite) o).getChannel().equals(getChannel());
  //    }
  //    return false;
  //  }
}
