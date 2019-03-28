package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class IdCommand extends AbstractCommand {

    /**
     * Returns the commands that will be executed on the chat.
     *
     * @return Command to be executed.
     */
    @Override
    public String getName() {
        return "id";
    }

    /**
     * Processes the data of the commands.
     *
     * @param update The received message.
     */
    @Override
    public void execute(Update update) {
        Long idChat = update.getMessage().getChatId();
        bot.sendMessage(idChat.toString(), idChat);
    }

    /**
     * Returns the help of the commands.
     *
     * @return The help.
     */
    @Override
    public String help() {
        return "Shows the ID of the current chat.";
    }
}
