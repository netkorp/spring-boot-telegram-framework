package com.github.netkorp.telegram.framework.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Contains the logic of a simple command. In addition, it could be used to identify the simple command.
 *
 * @see MultistageCommand
 */
public interface SimpleCommand extends Command {

    /**
     * Executes the command's logic taking parameters.
     *
     * @param update the received message.
     * @param args   the parameters passed to the command execution.
     */
    void execute(final Update update, String[] args);

    /**
     * Executes the command's logic without taking parameters.
     *
     * @param update the received message.
     */
    default void execute(final Update update) {
        this.execute(update, new String[]{});
    }
}
