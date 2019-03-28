package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.StringJoiner;

@Component
public class HelpCommand extends AbstractCommand {

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
        commandManager.getAvailableCommands().forEach(command -> stringJoiner.add(String.format("<b>%s:</b>\t\t%s", command.command(), command.help())));
        bot.sendMessage(stringJoiner.toString(), update.getMessage().getChatId(), true);
    }

    /**
     * Returns the help of the commands.
     *
     * @return The help.
     */
    @Override
    public String help() {
        return "Shows this message";
    }
}
