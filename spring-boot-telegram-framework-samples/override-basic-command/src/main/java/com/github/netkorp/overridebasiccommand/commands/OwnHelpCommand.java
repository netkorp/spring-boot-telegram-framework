package com.github.netkorp.overridebasiccommand.commands;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.StringJoiner;

@TelegramCommand(name = "assistance", free = true)
public class OwnHelpCommand extends AbstractSimpleCommand implements HelpCommand {

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
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
     * Returns the command's description, used to be displayed in help message.
     *
     * @return the command's description.
     */
    @Override
    public String description() {
        return "Displays an assistance message";
    }
}
