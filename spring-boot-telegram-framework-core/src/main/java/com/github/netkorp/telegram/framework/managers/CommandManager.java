package com.github.netkorp.telegram.framework.managers;

import com.github.netkorp.telegram.framework.annotations.CommandGroup;
import com.github.netkorp.telegram.framework.annotations.FreeCommand;
import com.github.netkorp.telegram.framework.bots.PollingTelegramBot;
import com.github.netkorp.telegram.framework.commands.interfaces.CloseCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.DoneCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class CommandManager {
    private final Map<String, Command> secureCommands;
    private final Map<String, Command> freeCommands;

    private final SortedMap<String, List<Command>> commandsByGroup;

    private final Map<Long, MultistageCommand> activeCommand;

    private String closeCommand;
    private String doneCommand;
    private String helpCommand;

    @Autowired
    public CommandManager(List<Command> commands, PollingTelegramBot bot, @Value("${telegram.commands.free}") String freeCommands) {
        this.secureCommands = new HashMap<>();
        this.freeCommands = new HashMap<>();

        this.commandsByGroup = new TreeMap<>();
        this.activeCommand = new HashMap<>();

        commands.forEach(command -> addCommand(command, bot, Arrays.asList(freeCommands.split(","))));
    }

    private void addCommand(Command command, PollingTelegramBot bot, List<String> freeCommandNames) {
        command.setBot(bot);
        command.setCommandManager(this);
        this.secureCommands.put(command.command(), command);

        if (isFreeCommand(command, freeCommandNames)) {
            this.freeCommands.put(command.command(), command);
        }

        if (command instanceof CloseCommand) {
            closeCommand = command.command();
        } else if (command instanceof DoneCommand) {
            doneCommand = command.command();
        } else if (command instanceof HelpCommand) {
            helpCommand = command.command();
        }

        // Groups
        String group = command.getClass().isAnnotationPresent(CommandGroup.class) ?
                command.getClass().getAnnotation(CommandGroup.class).value() : "";

        List<Command> commandList = this.commandsByGroup.getOrDefault(group, new LinkedList<>());
        commandList.add(command);

        this.commandsByGroup.put(group, commandList);
    }

    private boolean isFreeCommand(Command command, List<String> freeCommandNames) {
        return freeCommandNames.contains(command.getName())
                || freeCommandNames.contains(command.command())
                || command.getClass().isAnnotationPresent(FreeCommand.class);
    }

    /**
     * Returns the {@link Command} instance from a given getName.
     *
     * @param command The commands as {@link String}.
     * @return The {@link Command} instance.
     * @throws CommandNotFound If the given name is not related to any commands.
     */
    public Command getSecureCommand(String command) throws CommandNotFound {
        if (!this.secureCommands.containsKey(command)) {
            throw new CommandNotFound();
        }

        return this.secureCommands.get(command);
    }

    public Command getFreeCommand(String command) throws CommandNotFound {
        if (!this.freeCommands.containsKey(command)) {
            throw new CommandNotFound();
        }

        return this.freeCommands.get(command);
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

    public Command getCloseCommand() throws CommandNotFound {
        return getSecureCommand(this.closeCommand);
    }

    public Command getDoneCommand() throws CommandNotFound {
        return getSecureCommand(this.doneCommand);
    }

    public Command getHelpCommand() throws CommandNotFound {
        return getSecureCommand(this.helpCommand);
    }

    /**
     * Returns the list with the available commands.
     *
     * @return Available commands.
     */
    public SortedMap<String, List<Command>> getCommandsByGroup() {
        return commandsByGroup;
    }
}
