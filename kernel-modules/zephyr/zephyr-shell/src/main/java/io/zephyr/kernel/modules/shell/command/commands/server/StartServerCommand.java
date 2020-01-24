package io.zephyr.kernel.modules.shell.command.commands.server;

/**
 * Scenarios:
 *
 * <p>1. Interactive mode: if server isn't started, we would have to create a child thread or
 * process, which would be destroyed upon exiting interactive mode 2. Server mode: server stopped,
 * server process has exited, would have to drop into (1)
 *
 * <p>conclusion: starting the server isn't really feasible. Leaving this class as an explanation
 */
public class StartServerCommand {}
