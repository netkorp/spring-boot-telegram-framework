package com.github.netkorp.telegram.framework.commands.abstracts;

import com.github.netkorp.telegram.framework.bots.PollingTelegramBot;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Stores the basic components that the command needs.
 */
public abstract class AbstractCommand implements Command {

    /**
     * The bot that the command can use to share information with Telegram.
     */
    protected PollingTelegramBot bot;

    /**
     * The component for managing all of the commands available in the bot.
     */
    protected CommandManager commandManager;

    /**
     * Sets the Telegram bot to be used.
     *
     * @param bot the Telegram bot.
     */
    @Override
    @Autowired
    public void setBot(PollingTelegramBot bot) {
        this.bot = bot;
    }

    /**
     * Sets the {@link CommandManager} to be used.
     *
     * @param commandManager the {@link CommandManager} instance.
     */
    @Override
    @Autowired
    public void setCommandManager(@Lazy CommandManager commandManager) {
        this.commandManager = commandManager;
    }
}
