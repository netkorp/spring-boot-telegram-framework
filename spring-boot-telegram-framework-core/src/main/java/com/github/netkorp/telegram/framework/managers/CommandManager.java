package com.github.netkorp.telegram.framework.managers;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CommandManager {
    private final Map<String, Command> commands;
    private final Map<String, Command> freeCommands;

    private final Map<Long, MultistageCommand> activeCommand;

    private String closeCommand;
    private String doneCommand;
    private String helpCommand;

    @Autowired
    public CommandManager(List<Command> commands, PollingTelegramBot bot, @Value("${telegram.commands.free}") String freeCommands) {
        this.commands = new HashMap<>();
        this.freeCommands = new HashMap<>();

        this.activeCommand = new HashMap<>();

        commands.stream()
                .filter(item -> item.getClass().isAnnotationPresent(TelegramCommand.class))
                .forEach(command -> addCommand(command, bot, Arrays.asList(freeCommands.split(","))));
    }

    private void addCommand(Command command, PollingTelegramBot bot, List<String> freeCommandNames) {
        command.setBot(bot);
        command.setCommandManager(this);
        this.commands.put(getCommand(command), command);

        if (isFreeCommand(command, freeCommandNames)) {
            this.freeCommands.put(getCommand(command), command);
        }

        if (command instanceof CloseCommand) {
            closeCommand = getCommand(command);
        } else if (command instanceof DoneCommand) {
            doneCommand = getCommand(command);
        } else if (command instanceof HelpCommand) {
            helpCommand = getCommand(command);
        }
    }

    private boolean isFreeCommand(Command command, List<String> freeCommandNames) {
        return freeCommandNames.contains(getCommandName(command))
                || freeCommandNames.contains(getCommand(command))
                || command.getClass().getAnnotation(TelegramCommand.class).free();
    }

    @SuppressWarnings("WeakerAccess")
    public static String getCommandName(Command command) {
        return command.getClass().getAnnotation(TelegramCommand.class).name();
    }

    public static String getCommand(Command command) {
        String commandName = getCommandName(command);
        return commandName.startsWith("/") ? commandName : String.format("/%s", commandName);
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

    /**
     * Returns the {@link CloseCommand} if it exists.
     *
     * @return The {@link CloseCommand} instance.
     */
    public Optional<Command> getCloseCommand() {
        try {
            return Optional.ofNullable(getCommand(this.closeCommand));
        } catch (CommandNotFound commandNotFound) {
            return Optional.empty();
        }
    }

    /**
     * Returns the {@link DoneCommand} if it exists.
     *
     * @return The {@link DoneCommand} instance.
     */
    public Optional<Command> getDoneCommand() {
        try {
            return Optional.ofNullable(getCommand(this.doneCommand));
        } catch (CommandNotFound commandNotFound) {
            return Optional.empty();
        }
    }

    /**
     * Returns the {@link HelpCommand} if it exists.
     *
     * @return The {@link HelpCommand} instance.
     */
    public Optional<Command> getHelpCommand() {
        try {
            return Optional.ofNullable(getCommand(this.helpCommand));
        } catch (CommandNotFound commandNotFound) {
            return Optional.empty();
        }
    }

    /**
     * Returns a list with the available commands.
     *
     * @return Available commands.
     */
    public Collection<Command> getAvailableCommands() {
        return commands.values();
    }

    /**
     * Returns a list with the available free commands.
     *
     * @return Available free commands.
     */
    public Collection<Command> getAvailableFreeCommands() {
        return freeCommands.values();
    }
}
