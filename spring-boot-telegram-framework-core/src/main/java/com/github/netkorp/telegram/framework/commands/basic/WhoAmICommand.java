package com.github.netkorp.telegram.framework.commands.basic;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand;
import com.github.netkorp.telegram.framework.condition.ExcludeCondition;
import org.springframework.context.annotation.Conditional;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Shows the chat identification for user who invoked the command.
 */
@TelegramCommand(name = "whoami", group = "Basic", secure = false)
@Conditional(ExcludeCondition.class)
public final class WhoAmICommand extends AbstractSimpleCommand {

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     * @param args   the parameters passed to the command execution.
     */
    @Override
    public void execute(Update update, String[] args) {
        Long idChat = update.getMessage().getChatId();
        bot.sendMessage(idChat.toString(), idChat);
    }

    /**
     * Returns the command's description, used to be displayed in help message.
     *
     * @return the command's description.
     */
    @Override
    public String description() {
        return "Shows the ID of the current chat.";
    }
}
