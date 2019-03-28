package com.github.netkorp.telegram.framework.commands.interfaces;

import com.github.netkorp.telegram.framework.bots.PollingTelegramBot;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {

    /**
     * Returns the commands that will be executed on the chat.
     *
     * @return Command to be executed.
     */
    String getName();

    /**
     *
     * @return
     */
    String command();

    /**
     * Defines the Telegram bots to be used.
     *
     * @param bot Telegram bots.
     */
    void setBot(final PollingTelegramBot bot);

    /**
     * Defines the getName manager to be used.
     *
     * @param commandManager Command manager.
     */
    void setCommandManager(CommandManager commandManager);

    /**
     * Processes the data of the commands.
     *
     * @param update The received message.
     */
    void execute(final Update update);

    /**
     * Returns the help of the commands.
     *
     * @return The help.
     */
    String help();
}
