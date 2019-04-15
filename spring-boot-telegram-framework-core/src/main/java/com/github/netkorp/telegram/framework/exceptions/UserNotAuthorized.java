package com.github.netkorp.telegram.framework.exceptions;

/**
 * Indicates that the command is not available for a user.
 */
public class UserNotAuthorized extends Exception {

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public UserNotAuthorized() {
        super("This command is not available for you");
    }
}
