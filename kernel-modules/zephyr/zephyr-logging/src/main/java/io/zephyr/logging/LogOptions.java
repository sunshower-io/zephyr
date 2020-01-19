package io.zephyr.logging;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.AbstractValidatable;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

import java.io.File;

public class LogOptions extends AbstractValidatable<LogOptions> implements Options<LogOptions> {

    /**
     * Specify the home directory for Sunshower.io. Sunshower data is stored here. For clustered
     * Sunshower.io kernels, this should be a distributed directory unless a data-distribution module
     * is installed
     */
    @Getter
    @Setter
    @CommandLine.Option(names = {"-h", "--home-directory"})
    private File homeDirectory;

}
