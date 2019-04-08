package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import com.github.netkorp.telegram.framework.condition.ExcludeCondition;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import com.github.netkorp.telegram.framework.managers.SecurityManager;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Conditional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Displays the bot's help.
 */
@TelegramCommand(name = "help", group = "Basic")
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
     */
    @Override
    public void execute(Update update) {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        stringJoiner.add("You can control me by sending these commands:");
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
        commands.forEach(command -> stringJoiner.add(String.format("%s - %s", CommandManager.getCommand(command), command.description())));
        return System.lineSeparator() + stringJoiner.toString();
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
            // Groups
            String group = command.getClass().getAnnotation(TelegramCommand.class).group();

            List<Command> commandList = commandsByGroup.getOrDefault(group, new LinkedList<>());
            commandList.add(command);

            commandsByGroup.put(group, commandList);
        });

        return commandsByGroup;
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

        return commandManager.getAvailableFreeCommands();
    }

    /**
     * Returns the command's description, used to be displayed in help message.
     *
     * @return the command's description.
     */
    @Override
    public String description() {
        return "Shows this message";
    }
}
