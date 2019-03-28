package com.github.netkorp.telegram.framework.commands.multistage;

import com.github.netkorp.telegram.framework.bots.PollingTelegramBot;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CloseCommand extends AbstractCommand {

    /**
     * Returns the commands that will be executed on the chat.
     *
     * @return Command to be executed.
     */
    @Override
    public String getName() {
        return "close";
    }

    /**
     * Processes the data of the commands.
     *
     * @param update The received message.
     */
    @Override
    public void execute(Update update) {
        final Long idChat = update.getMessage().getChatId();

        try {
            if (commandManager.getActiveCommand(idChat).close(update)) {
                commandManager.removeActiveCommand(idChat);
            }
        } catch (CommandNotActive commandNotActive) {
            bot.sendMessage(commandNotActive.getMessage(), idChat);
            try {
                commandManager.getCommand(PollingTelegramBot.HELP_COMMAND_NAME).execute(update);
            } catch (CommandNotFound commandNotFound) {
                // Do nothing
            }
        }
    }

    /**
     * Returns the help of the commands.
     *
     * @return The help.
     */
    @Override
    public String help() {
        return "Closes the current task. There should be an active commands before.";
    }
}
