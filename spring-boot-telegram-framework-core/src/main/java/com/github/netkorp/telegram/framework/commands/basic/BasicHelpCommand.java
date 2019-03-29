package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.annotations.CommandGroup;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.StringJoiner;

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
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("You can control me by sending these commands:\n");
        commandManager.getCommandsByGroup().forEach((group, commands) -> {
            if (!Strings.isEmpty(group)) {
                stringJoiner.add(String.format("\n<b>%s</b>", group));
            }
            commands.forEach(command -> stringJoiner.add(String.format("%s - %s", command.command(), command.description())));
        });
        bot.sendMessage(stringJoiner.toString(), update.getMessage().getChatId(), true);
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
