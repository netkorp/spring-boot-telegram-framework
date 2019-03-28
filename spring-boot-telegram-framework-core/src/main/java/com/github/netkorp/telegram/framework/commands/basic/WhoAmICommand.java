package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.annotations.CommandGroup;
import com.github.netkorp.telegram.framework.annotations.FreeCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@CommandGroup("Basic")
@FreeCommand
public class WhoAmICommand extends AbstractCommand {

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
     * Returns the description of the commands.
     *
     * @return The description.
     */
    @Override
    public String description() {
        return "Shows the ID of the current chat.";
    }
}
