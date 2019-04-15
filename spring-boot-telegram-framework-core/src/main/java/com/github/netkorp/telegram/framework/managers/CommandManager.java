package com.github.netkorp.telegram.framework.managers;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.commands.multistage.MultistageCloseCommand;
import com.github.netkorp.telegram.framework.commands.multistage.MultistageDoneCommand;
import com.github.netkorp.telegram.framework.properties.CommandProperties;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides the component for managing all of the commands available in the bot.
 * It includes the management of non-secure commands, active command, basic commands and
 * those commands that are involved in the multistage command flow.
 */
@SuppressWarnings("WeakerAccess")
@Component
public class CommandManager {

    /**
     * The list of the available commands into a map to get a quick access from its name.
     */
    private final Map<String, Command> commands;

    /**
     * The list of the non-secure commands into a map to get a quick access from its name.
     */
    private final Map<String, Command> nonSecureCommands;

    /**
     * The command that is active for each user.
     */
    private final Map<Long, MultistageCommand> activeCommand;

    /**
     * The properties of the commands.
     */
    private final CommandProperties commandProperties;

    /**
     * The command that closes an active conversation with the bot, indicating to the active command that the conversation is closed.
     * It is kept here for a quick access to the close command.
     */
    private MultistageCloseCommand closeCommand;

    /**
     * The command that closes an active conversation with the bot, indicating to the active command that the conversation is done.
     * It is kept here for a quick access to the done command.
     */
    private MultistageDoneCommand doneCommand;

    /**
     * The command that shows the help of the bot.
     * It is kept here for a quick access to the help command.
     */
    private HelpCommand helpCommand;

    /**
     * Constructs a new {@link CommandManager} instance with the list of available {@link Command}
     * and the properties of the commands.
     *
     * @param commands          the list of available {@link Command}.
     * @param commandProperties the properties of the commands.
     */
    @Autowired
    public CommandManager(List<Command> commands, CommandProperties commandProperties) {
        this.commands = new HashMap<>();
        this.nonSecureCommands = new HashMap<>();

        this.activeCommand = new HashMap<>();
        this.commandProperties = commandProperties;

        commands.stream()
                .filter(item -> item.getClass().isAnnotationPresent(TelegramCommand.class))
                .forEach(this::addCommand);
    }

    /**
     * Adds the command to the list of available/non-secure commands and sets the commands for
     * {@link #closeCommand}, {@link #doneCommand} and {@link #helpCommand}.
     *
     * @param command the command to be added.
     * @see #commands
     * @see #nonSecureCommands
     * @see #closeCommand
     * @see #doneCommand
     * @see #helpCommand
     */
    private void addCommand(Command command) {
        this.commands.put(getCommandFullName(command), command);

        // Just for keeping a reference of the non-secure commands
        if (commandProperties.getNonSecure().contains(getCommandName(command))
                || commandProperties.getNonSecure().contains(getCommandFullName(command))
                || !command.getClass().getAnnotation(TelegramCommand.class).secure()) {
            this.nonSecureCommands.put(getCommandFullName(command), command);
        }

        if (command instanceof MultistageCloseCommand) {
            closeCommand = ((MultistageCloseCommand) command);
        } else if (command instanceof MultistageDoneCommand) {
            doneCommand = ((MultistageDoneCommand) command);
        } else if (command instanceof HelpCommand) {
            helpCommand = ((HelpCommand) command);
        }
    }

    /**
     * Returns {@code true} if the command is non-secure.
     *
     * @param command the command to be processed.
     * @return {@code true} if the command is non-secure; {@code false} otherwise.
     */
    public boolean isNonSecureCommand(Command command) {
        return this.nonSecureCommands.containsKey(getCommandFullName(command));
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
     * @param commandName the command's name (the same name declared on {@link TelegramCommand#name()})
     * @return the name of the command.
     */
    public static String getCommandFullName(String commandName) {
        return commandName.startsWith("/") ? commandName : String.format("/%s", commandName);
    }

    /**
     * Returns the name of the command. It includes the slash (/).
     *
     * @param command the command from which the name will be identified.
     * @return the name of the command.
     */
    public static String getCommandFullName(Command command) {
        return getCommandFullName(getCommandName(command));
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
     * Returns the {@link MultistageCloseCommand} if it exists.
     *
     * @return the {@link MultistageCloseCommand} instance.
     */
    public Optional<MultistageCloseCommand> getCloseCommand() {
        return Optional.ofNullable(this.closeCommand);
    }

    /**
     * Returns the {@link MultistageDoneCommand} if it exists.
     *
     * @return the {@link MultistageDoneCommand} instance.
     */
    public Optional<MultistageDoneCommand> getDoneCommand() {
        return Optional.ofNullable(this.doneCommand);
    }

    /**
     * Returns the {@link HelpCommand} if it exists.
     *
     * @return the {@link HelpCommand} instance.
     */
    public Optional<HelpCommand> getHelpCommand() {
        return Optional.ofNullable(this.helpCommand);
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
     * Returns a list with the available non-secure commands.
     *
     * @return the available non-secure commands.
     */
    public Collection<Command> getAvailableNonSecureCommands() {
        return nonSecureCommands.values();
    }
}
