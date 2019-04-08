package com.github.netkorp.telegram.framework.commands.interfaces;

import com.github.netkorp.telegram.framework.bots.PollingTelegramBot;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Contains the logic of a command to be executed by the users.
 */
public interface Command {

    /**
     * Sets the Telegram bot to be used.
     *
     * @param bot the Telegram bot.
     */
    void setBot(final PollingTelegramBot bot);

    /**
     * Sets the {@link CommandManager} to be used.
     *
     * @param commandManager the {@link CommandManager} instance.
     */
    void setCommandManager(final CommandManager commandManager);

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
