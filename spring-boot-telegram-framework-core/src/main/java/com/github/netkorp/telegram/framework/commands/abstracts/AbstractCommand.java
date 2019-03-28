package com.github.netkorp.telegram.framework.commands.abstracts;

import com.github.netkorp.telegram.framework.bots.PollingTelegramBot;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.managers.CommandManager;

public abstract class AbstractCommand implements Command {

    protected PollingTelegramBot bot;
    protected CommandManager commandManager;

    /**
     * Defines the Telegram bots to be used.
     *
     * @param bot Telegram bots.
     */
    @Override
    public void setBot(PollingTelegramBot bot) {
        this.bot = bot;
    }

    /**
     * Defines the getName manager to be used.
     *
     * @param commandManager Command manager.
     */
    @Override
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }
}
