package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.annotations.CommandGroup;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.StringJoiner;

@Component
@CommandGroup("Basic")
@ConditionalOnSingleCandidate(com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand.class)
public class HelpCommand extends AbstractCommand implements com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand {

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
