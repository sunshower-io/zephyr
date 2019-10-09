package io.sunshower.kernel;

import lombok.AllArgsConstructor;

public class PluginRegistrationException extends KernelException {


    private final Object source;
    public PluginRegistrationException(Exception cause, String message, Object source) {
        super(message, cause);
        this.source = source;
    }

}
