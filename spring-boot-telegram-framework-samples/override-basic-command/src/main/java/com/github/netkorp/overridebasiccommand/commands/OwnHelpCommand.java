package com.github.netkorp.overridebasiccommand.commands;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.StringJoiner;

@TelegramCommand(name = "assistance", free = true)
public class OwnHelpCommand extends AbstractCommand implements HelpCommand {

    /**
     * Processes the data of the commands.
     *
     * @param update The received message.
     */
    @Override
    public void execute(Update update) {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        stringJoiner.add("Commands:");
        commandManager.getAvailableFreeCommands()
                .forEach(command -> stringJoiner.add(String.format("%s - <b>%s</b>", CommandManager.getCommand(command), command.description())));
        bot.sendMessage(stringJoiner.toString(), update.getMessage().getChatId(), true);
    }

    /**
     * Returns the description of the commands.
     *
     * @return The description.
     */
    @Override
    public String description() {
        return "Displays an assistance message";
    }
}
