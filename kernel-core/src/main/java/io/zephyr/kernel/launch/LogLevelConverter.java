package io.zephyr.kernel.launch;

import picocli.CommandLine;

import java.util.logging.Level;

public class LogLevelConverter implements CommandLine.ITypeConverter<Level> {

    @Override
    public Level convert(String value) throws Exception {
        return Level.parse(value);
    }
}
