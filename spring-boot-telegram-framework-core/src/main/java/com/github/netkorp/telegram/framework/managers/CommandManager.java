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

/**
 * Provides the component for managing all of the commands available in the bot.
 * It includes the management of free commands, active command, basic commands and
 * those commands that are involved in the multistage command flow.
 */
@Component
public class CommandManager {

    /**
     * The list of the secured commands into a map to get a quick access from its name.
     */
    private final Map<String, Command> commands;

    /**
     * The list of the free commands into a map to get a quick access from its name.
     */
    private final Map<String, Command> freeCommands;

    /**
     * The command that is active for each user.
     */
    private final Map<Long, MultistageCommand> activeCommand;

    /**
     * The name used for invoking the command that closes an active conversation with the bot indicating to the active command that the conversation is closed.
     * It is kept here for a quick access to the close command.
     */
    private String closeCommand;

    /**
     * The name used for invoking the command that closes an active conversation with the bot indicating to the active command that the conversation is done.
     * It is kept here for a quick access to the done command.
     */
    private String doneCommand;

    /**
     * The name used for invoking the command that shows the help of the bot.
     * It is kept here for a quick access to the help command.
     */
    private String helpCommand;

    /**
     * Constructs a new {@link CommandManager} instance with the list of available {@link Command},
     * the {@link PollingTelegramBot} instance and the list (as a string where each name is separated by commas)
     * of the free commands.
     *
     * @param commands     the list of available {@link Command}.
     * @param bot          the bot instance.
     * @param freeCommands the free command list.
     */
    @Autowired
    public CommandManager(List<Command> commands, PollingTelegramBot bot, @Value("${telegram.commands.free}") String freeCommands) {
        this.commands = new HashMap<>();
        this.freeCommands = new HashMap<>();

        this.activeCommand = new HashMap<>();

        commands.stream()
                .filter(item -> item.getClass().isAnnotationPresent(TelegramCommand.class))
                .forEach(command -> addCommand(command, bot, Arrays.asList(freeCommands.split(","))));
    }

    /**
     * Adds the command to the list of available/free commands and
     * sets the name for {@link #closeCommand}, {@link #doneCommand} and {@link #helpCommand}.
     *
     * @param command          the command to be added.
     * @param bot              the bot instance.
     * @param freeCommandNames the free command list.
     * @see #commands
     * @see #freeCommands
     * @see #closeCommand
     * @see #doneCommand
     * @see #helpCommand
     */
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

    /**
     * Returns {@code true} if the command is free.
     *
     * @param command          the command to be processed.
     * @param freeCommandNames the list of free commands.
     * @return {@code true} if the command is free; {@code false} otherwise.
     */
    private boolean isFreeCommand(Command command, List<String> freeCommandNames) {
        return freeCommandNames.contains(getCommandName(command))
                || freeCommandNames.contains(getCommand(command))
                || command.getClass().getAnnotation(TelegramCommand.class).free();
    }

    /**
     * Returns the name of the command (the same name declared on {@link TelegramCommand#name()}).
     * The name may include the slash (/).
     *
     * @param command the command from which the name will be identified.
     * @return the name of the command.
     */
    @SuppressWarnings("WeakerAccess")
    public static String getCommandName(Command command) {
        return command.getClass().getAnnotation(TelegramCommand.class).name();
    }

    /**
     * Returns the name of the command. It includes the slash (/).
     *
     * @param command the command from which the name will be identified.
     * @return the name of the command.
     */
    public static String getCommand(Command command) {
        String commandName = getCommandName(command);
        return commandName.startsWith("/") ? commandName : String.format("/%s", commandName);
    }

    /**
     * Returns the {@link Command} instance from the command name.
     *
     * @param command the command name.
     * @return the {@link Command} instance.
     * @throws CommandNotFound if the name is not related to any commands.
     */
    public Command getCommand(String command) throws CommandNotFound {
        if (!this.commands.containsKey(command)) {
            throw new CommandNotFound();
        }

        return this.commands.get(command);
    }

    /**
     * Returns the free {@link Command} instance from the command name.
     *
     * @param command the command name.
     * @return the free {@link Command} instance.
     * @throws CommandNotFound if the name is not related to any commands.
     */
    public Command getFreeCommand(String command) throws CommandNotFound {
        if (!this.freeCommands.containsKey(command)) {
            throw new CommandNotFound();
        }

        return this.freeCommands.get(command);
    }

    /**
     * Sets the multistage command as the active one.
     *
     * @param idChat  the chat identification of the user for whom the multistage command will be active.
     * @param command the command to activate.
     */
    public void setActiveCommand(final Long idChat, final MultistageCommand command) {
        this.activeCommand.put(idChat, command);
    }

    /**
     * Gets the active command for the user.
     *
     * @param idChat the chat identification of the user.
     * @return the active command.
     * @throws CommandNotActive if there is no an active command.
     */
    public MultistageCommand getActiveCommand(Long idChat) throws CommandNotActive {
        if (!hasActiveCommand(idChat)) {
            throw new CommandNotActive();
        }

        return activeCommand.get(idChat);
    }

    /**
     * Removes the active command.
     *
     * @param idChat the chat identification of the user.
     */
    public void removeActiveCommand(Long idChat) {
        activeCommand.remove(idChat);
    }

    /**
     * Returns {@code true} if there is an active command.
     *
     * @param idChat the chat identification of the user.
     * @return {@code true} if there is an active command; {@code false} otherwise.
     */
    public boolean hasActiveCommand(Long idChat) {
        return activeCommand.containsKey(idChat);
    }

    /**
     * Returns the {@link CloseCommand} if it exists.
     *
     * @return the {@link CloseCommand} instance.
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
     * @return the {@link DoneCommand} instance.
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
     * @return the {@link HelpCommand} instance.
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
     * @return the available commands.
     */
    public Collection<Command> getAvailableCommands() {
        return commands.values();
    }

    /**
     * Returns a list with the available free commands.
     *
     * @return the available free commands.
     */
    public Collection<Command> getAvailableFreeCommands() {
        return freeCommands.values();
    }
}
