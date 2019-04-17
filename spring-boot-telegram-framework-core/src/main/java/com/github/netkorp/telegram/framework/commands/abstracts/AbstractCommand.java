package com.github.netkorp.telegram.framework.commands.abstracts;

import com.github.netkorp.telegram.framework.bots.PollingTelegramBot;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;

/**
 * Stores the basic components that the command needs.
 */
abstract class AbstractCommand implements Command {

    /**
     * The bot that the command can use to share information with Telegram.
     */
    protected PollingTelegramBot bot;

    /**
     * The component for managing all of the commands available in the bot.
     */
    protected CommandManager commandManager;

    /**
     * The component for resolving messages
     */
    protected MessageSource messageSource;

    /**
     * Sets the Telegram bot to be used.
     *
     * @param bot the Telegram bot.
     */
    @Autowired
    public void setBot(PollingTelegramBot bot) {
        this.bot = bot;
    }

    /**
     * Sets the {@link CommandManager} to be used.
     *
     * @param commandManager the {@link CommandManager} instance.
     */
    @Autowired
    public void setCommandManager(@Lazy CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Sets the {@link MessageSource} to be used.
     *
     * @param messageSource the {@link MessageSource} instance.
     */
    @Autowired
    public void setMessageSource(@Qualifier("TelegramFrameworkMessageSource") MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}
