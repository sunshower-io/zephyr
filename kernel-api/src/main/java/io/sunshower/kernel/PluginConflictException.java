package io.sunshower.kernel;

public class PluginConflictException extends PluginRegistrationException {

    public PluginConflictException(Exception cause, String message, Object source) {
        super(cause, message, source);
    }
}
