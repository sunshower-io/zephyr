package io.zephyr.common;

import io.zephyr.kernel.extensions.EntryPoint;
import java.util.Map;
import java.util.function.Supplier;
import lombok.val;
import picocli.CommandLine;

public class Options {
  private Options() {
    throw new IllegalStateException("no options 4 u");
  }

  public static <T extends io.zephyr.kernel.Options<T>> T create(
      Supplier<T> ctor, Map<EntryPoint.ContextEntries, Object> ctx) {

    val result = ctor.get();
    val args = (String[]) ctx.get(EntryPoint.ContextEntries.ARGS);
    val commandLine = new CommandLine(result).setUnmatchedArgumentsAllowed(true);
    val parseResult = commandLine.parseArgs(args);
    //    System.out.println("UNMATCHED " + parseResult.unmatched());
    //    ctx.put(EntryPoint.ContextEntries.ARGS, parseResult.unmatched().toArray(new String[0]));
    return result;
  }
}
