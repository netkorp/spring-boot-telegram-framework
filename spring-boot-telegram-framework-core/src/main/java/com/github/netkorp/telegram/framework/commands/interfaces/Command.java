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
    default String getName() {
        return this.getClass().getSimpleName().toLowerCase().replace("command", "");
    }

    /**
     *
     * @return
     */
    default String command() {
        return getName().startsWith("/") ? getName() : String.format("/%s", getName());
    }

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
     * Returns the description of the commands.
     *
     * @return The description.
     */
    String description();
}
