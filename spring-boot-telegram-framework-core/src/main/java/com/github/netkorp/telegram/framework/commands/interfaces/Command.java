package com.github.netkorp.telegram.framework.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Contains the logic of a command to be executed by the users.
 */
public interface Command {

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     */
    void execute(final Update update);

    /**
     * Returns the command's description, used to be displayed in help message.
     *
     * @return the command's description.
     */
    String description();
}
