package com.github.netkorp.simplecommand.commands;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import org.telegram.telegrambots.meta.api.objects.Update;

@TelegramCommand(name = "name")
public class NameCommand extends AbstractCommand {

    /**
     * Processes the data of the commands.
     *
     * @param update The received message.
     */
    @Override
    public void execute(Update update) {
        bot.sendMessage(update.getMessage().getChat().getFirstName(), update.getMessage().getChatId());
    }

    /**
     * Returns the description of the commands.
     *
     * @return The description.
     */
    @Override
    public String description() {
        return "It tells you what your first name is";
    }
}
