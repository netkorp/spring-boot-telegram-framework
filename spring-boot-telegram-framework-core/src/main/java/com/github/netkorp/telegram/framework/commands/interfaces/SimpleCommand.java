package com.github.netkorp.telegram.framework.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Contains the logic of a simple command. In addition, it could be used to identify the simple command.
 *
 * @see MultistageCommand
 */
public interface SimpleCommand extends Command {

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     * @param args   the parameters passed to the command execution.
     */
    void execute(final Update update, String[] args);
}
