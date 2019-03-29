package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.annotations.CommandGroup;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;

@Component
@CommandGroup("Basic")
@ConditionalOnSingleCandidate(HelpCommand.class)
public class BasicHelpCommand extends AbstractCommand implements HelpCommand {

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
        stringJoiner.add("You can control me by sending these commands:" + System.lineSeparator());
        commandsByGroup().forEach((group, commands) -> {
            if (!Strings.isEmpty(group)) {
                stringJoiner.add(String.format("%n<b>%s</b>", group));
            }
            commands.forEach(command -> stringJoiner.add(String.format("%s - %s", command.command(), command.description())));
        });
        bot.sendMessage(stringJoiner.toString(), update.getMessage().getChatId(), true);
    }

    /**
     * Groups the commands by groups.
     *
     * @return The grouped commands.
     */
    private SortedMap<String, List<Command>> commandsByGroup() {
        SortedMap<String, List<Command>> commandsByGroup = new TreeMap<>();

        commandManager.getAvailableCommands().forEach(command -> {
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
     * Returns the description of the commands.
     *
     * @return The description.
     */
    @Override
    public String description() {
        return "Shows this message";
    }
}
