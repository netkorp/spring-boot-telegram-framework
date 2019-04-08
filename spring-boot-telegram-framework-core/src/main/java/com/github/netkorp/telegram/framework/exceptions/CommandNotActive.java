package com.github.netkorp.telegram.framework.exceptions;

/**
 * Indicates that there is no active command.
 */
public class CommandNotActive extends Exception {

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public CommandNotActive() {
        super("There is no an active command");
    }
}
