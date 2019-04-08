package com.github.netkorp.simplecommand.commands;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import org.telegram.telegrambots.meta.api.objects.Update;

@TelegramCommand(name = "name")
public class NameCommand extends AbstractCommand {

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     */
    @Override
    public void execute(Update update) {
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
