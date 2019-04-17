package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import com.github.netkorp.telegram.framework.condition.ExcludeCondition;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import com.github.netkorp.telegram.framework.managers.SecurityManager;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.i18n.LocaleContextHolder;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Displays the bot's help.
 */
@TelegramCommand(name = "help", group = "commands.groups.help", description = "commands.description.help")
@Conditional(ExcludeCondition.class)
@ConditionalOnSingleCandidate(HelpCommand.class)
public class BasicHelpCommand extends AbstractSimpleCommand implements HelpCommand {

    /**
     * The component to know which user is authorized.
     */
    private final SecurityManager securityManager;

    /**
     * Constructs a new {@link BasicHelpCommand} instance with the {@link SecurityManager} component instance.
     *
     * @param securityManager the {@link SecurityManager} component instance.
     */
    @Autowired
    public BasicHelpCommand(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     * @param args   the parameters passed to the command execution.
     */
    @Override
    public void execute(final Update update, String[] args) {
        try {
            if (args.length == 0) {
                execute(update);
                return;
            }

            StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());

            for (String arg : args) {
                try {
                    stringJoiner.add(helpForCommand(commandManager.getCommand(CommandManager.getCommandFullName(arg))));
                } catch (CommandNotFound commandNotFound) {
                    bot.sendMessage(String.format("%s: %s", commandNotFound.getMessage(), arg), update.getMessage().getChatId(), true);
                    execute(update);
                    throw commandNotFound;
                }
            }

            bot.sendMessage(stringJoiner.toString(), update.getMessage().getChatId(), true);
        } catch (CommandNotFound commandNotFound) {
            // Do nothing
        }
    }

    /**
     * Executes the command's logic without taking parameters.
     *
     * @param update the received message.
     */
    @Override
    public void execute(Update update) {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());

        stringJoiner.add(String.format("%s:", messageSource.getMessage("commands.basic.help.title", null,
                LocaleContextHolder.getLocale())));

        commandsByGroup(getAvailableCommands(update.getMessage().getChatId()))
                .forEach((group, commands) -> stringJoiner.add(helpForGroup(group, commands)));

        bot.sendMessage(stringJoiner.toString(), update.getMessage().getChatId(), true);
    }

    /**
     * Returns the help for the group of commands.
     *
     * @param group    the name of the group.
     * @param commands the commands into the group.
     * @return the help for the group.
     */
    private String helpForGroup(String group, List<Command> commands) {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        if (!Strings.isEmpty(group)) {
            stringJoiner.add(String.format("<b>%s</b>", group));
        }
        commands.forEach(command -> stringJoiner.add(helpForCommand(command)));
        return System.lineSeparator() + stringJoiner.toString();
    }

    /**
     * Returns the help for a single command.
     *
     * @param command the command.
     * @return the help of the command.
     */
    private String helpForCommand(Command command) {
        StringJoiner stringJoiner = new StringJoiner(", ");
        CommandManager.getCommandFullNames(command).forEach(stringJoiner::add);
        return String.format("%s - %s", stringJoiner.toString(), getDescription(command));
    }

    /**
     * Organizes the available commands into groups.
     *
     * @param commands the list with all the available commands.
     * @return the available commands in groups sorted by the group's name.
     */
    private SortedMap<String, List<Command>> commandsByGroup(Collection<Command> commands) {
        SortedMap<String, List<Command>> commandsByGroup = new TreeMap<>();

        commands.forEach(command -> {
            String group = getGroupName(command);

            List<Command> commandList = commandsByGroup.getOrDefault(group, new LinkedList<>());
            commandList.add(command);

            commandsByGroup.put(group, commandList);
        });

        return commandsByGroup;
    }

    /**
     * Cleans the class name by removing the word "command" from it.
     *
     * @param commandClass the class to be cleaned.
     * @return the cleaned class name.
     */
    private String cleanCommandClassName(Class<?> commandClass) {
        String className = commandClass.getSimpleName().toLowerCase();
        if (!"command".equals(className) && className.endsWith("command")) {
            className = className.substring(0, className.length() - 7);
        }

        return className;
    }

    /**
     * Returns the group's name of the command.
     *
     * @param command the command from which the group's name will be retrieved.
     * @return the group's name.
     */
    private String getGroupName(Command command) {
        String groupName = command.getClass().getAnnotation(TelegramCommand.class).group().trim();

        // If there is no an explicit group, we'll try to generate a key to retrieve a message
        String key = "";
        if (groupName.isEmpty()) {
            key = "commands.groups." + cleanCommandClassName(command.getClass());
            groupName = key;
        }

        try {
            groupName = messageSource.getMessage(groupName, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException exception) {
            if (Objects.equals(key, groupName)) {
                groupName = "";
            }
        }

        return groupName;
    }

    /**
     * Returns the description of the command.
     *
     * @param command the command from which the description will be retrieved.
     * @return the command's description.
     */
    private String getDescription(Command command) {
        String description = command.getClass().getAnnotation(TelegramCommand.class).description().trim();

        // If there is no explicit description, we'll try to generate a description key to retrieve a message
        String key = "";
        if (description.isEmpty()) {
            key = "commands.description." + cleanCommandClassName(command.getClass());
            description = key;
        }

        try {
            description = messageSource.getMessage(description, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException exception) {
            if (Objects.equals(key, description)) {
                description = messageSource.getMessage("commands.basic.help.default-description", null, LocaleContextHolder.getLocale());
            }
        }

        return description;
    }

    /**
     * Returns the list of available commands for the user. It takes into account whether the user is authorized or not.
     *
     * @param chatId the identification of the user's chat.
     * @return the list of available commands.
     */
    private Collection<Command> getAvailableCommands(Long chatId) {
        if (securityManager.isAuthorized(chatId)) {
            return commandManager.getAvailableCommands();
        }

        return commandManager.getAvailableNonSecureCommands();
    }
}
