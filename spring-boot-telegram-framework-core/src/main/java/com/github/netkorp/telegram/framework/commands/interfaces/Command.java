package com.github.netkorp.telegram.framework.commands.interfaces;

/**
 * Contains the logic of a command to be executed by the users.
 */
public interface Command {

    /**
     * Returns the command's description, used to be displayed in help message.
     *
     * @return the command's description.
     */
    String description();
}
