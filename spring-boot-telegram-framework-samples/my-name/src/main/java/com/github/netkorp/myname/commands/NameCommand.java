package com.github.netkorp.myname.commands;

import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class NameCommand extends AbstractCommand {
    /**
     * Returns the commands that will be executed on the chat.
     *
     * @return Command to be executed.
     */
    @Override
    public String getName() {
        return "name";
    }

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
     * Returns the help of the commands.
     *
     * @return The help.
     */
    @Override
    public String help() {
        return "It tells you what your first name is";
    }
}
