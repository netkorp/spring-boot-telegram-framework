package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.annotations.CommandGroup;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import com.github.netkorp.telegram.framework.managers.SecurityManager;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;

@Component
@CommandGroup("Basic")
@ConditionalOnSingleCandidate(HelpCommand.class)
public class BasicHelpCommand extends AbstractCommand implements HelpCommand {

    private final SecurityManager securityManager;

    @Autowired
    public BasicHelpCommand(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Returns the commands that will be executed on the chat.
     *
     * @return Command to be executed.
     */
    @Override
    public String getName() {
        return "help";
    }

    /**
     * Processes the data of the commands.
     *
     * @param update The received message.
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
     * Returns the help for a given group of commands.
     *
     * @param group The name of the group.
     * @param commands The commands into the group.
     * @return The help.
     */
    private String helpForGroup(String group, List<Command> commands) {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        if (!Strings.isEmpty(group)) {
            stringJoiner.add(String.format("<b>%s</b>", group));
        }
        commands.forEach(command -> stringJoiner.add(String.format("%s - %s", command.command(), command.description())));
        return System.lineSeparator() + stringJoiner.toString();
    }

    /**
     * Groups the commands by groups.
     *
     * @param commands A list with all the commands.
     * @return The grouped commands.
     */
    private SortedMap<String, List<Command>> commandsByGroup(Collection<Command> commands) {
        SortedMap<String, List<Command>> commandsByGroup = new TreeMap<>();

        commands.forEach(command -> {
            // Groups
            String group = command.getClass().isAnnotationPresent(CommandGroup.class) ?
                    command.getClass().getAnnotation(CommandGroup.class).value() : "";

            List<Command> commandList = commandsByGroup.getOrDefault(group, new LinkedList<>());
            commandList.add(command);

            commandsByGroup.put(group, commandList);
        });

        return commandsByGroup;
    }

    /**
     * The commands to be shown on help.
     *
     * @param chatId The ID of current chat.
     * @return The commands.
     */
    private Collection<Command> getAvailableCommands(Long chatId) {
        if (securityManager.isAuthorized(chatId)) {
            return commandManager.getAvailableCommands();
        }

        return commandManager.getAvailableFreeCommands();
    }

    /**
     * Returns the description of the commands.
     *
     * @return The description.
     */
    @Override
    public String description() {
        return "Shows this message";
    }
}
