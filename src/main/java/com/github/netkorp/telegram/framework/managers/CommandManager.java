package com.github.netkorp.telegram.framework.managers;

import com.github.netkorp.telegram.framework.bots.PollingTelegramBot;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class CommandManager {
    private final SortedMap<String, Command> commands;
    private final Map<Long, MultistageCommand> activeCommand;

    @Autowired
    public CommandManager(List<Command> commands, PollingTelegramBot bot) {
        this.commands = new TreeMap<>();
        this.activeCommand = new HashMap<>();

        commands.forEach(command -> {
            command.setBot(bot);
            command.setCommandManager(this);
            this.commands.put(command.command(), command);
        });
    }

    /**
     * Returns the {@link Command} instance from a given getName.
     *
     * @param command The commands as {@link String}.
     * @return The {@link Command} instance.
     * @throws CommandNotFound If the given name is not related to any commands.
     */
    public Command getCommand(String command) throws CommandNotFound {
        if (!this.commands.containsKey(command)) {
            throw new CommandNotFound();
        }

        return this.commands.get(command);
    }

    public void setActiveCommand(final Long idChat, final MultistageCommand command) {
        this.activeCommand.put(idChat, command);
    }

    /**
     * Gets the active commands for a given user.
     *
     * @param idChat Chat id of the user.
     * @return The active commands.
     */
    public MultistageCommand getActiveCommand(Long idChat) throws CommandNotActive {
        if (!hasActiveCommand(idChat)) {
            throw new CommandNotActive();
        }

        return activeCommand.get(idChat);
    }

    /**
     * Removes the active commands.
     *
     * @param idChat Chat id of the user.
     */
    public void removeActiveCommand(Long idChat) {
        activeCommand.remove(idChat);
    }

    /**
     * Is there an active commands or not?
     *
     * @param idChat Chat id of the user.
     * @return If there is active commands or not.
     */
    public boolean hasActiveCommand(Long idChat) {
        return activeCommand.containsKey(idChat);
    }

    /**
     * Returns the list with the available commands.
     *
     * @return Available commands.
     */
    public Collection<Command> getAvailableCommands() {
        return commands.values();
    }
}
