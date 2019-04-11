package com.github.netkorp.simplecommand.commands;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand;
import org.telegram.telegrambots.meta.api.objects.Update;

@TelegramCommand(name = "name")
public class NameCommand extends AbstractSimpleCommand {

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     * @param args   the parameters passed to the command execution.
     */
    @Override
    public void execute(Update update, String[] args) {
        bot.sendMessage(update.getMessage().getChat().getFirstName(), update.getMessage().getChatId());
    }

    /**
     * Returns the command's description, used to be displayed in help message.
     *
     * @return the command's description.
     */
    @Override
    public String description() {
        return "It tells you what your first name is";
    }
}
