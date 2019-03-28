package com.github.netkorp.telegram.framework.commands.multistage;

import com.github.netkorp.telegram.framework.annotations.CommandGroup;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@CommandGroup("Multistage")
@ConditionalOnBean(MultistageCommand.class)
@ConditionalOnSingleCandidate(com.github.netkorp.telegram.framework.commands.interfaces.CloseCommand.class)
public class CloseCommand extends AbstractCommand implements com.github.netkorp.telegram.framework.commands.interfaces.CloseCommand {

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
                commandManager.getHelpCommand().execute(update);
            } catch (CommandNotFound commandNotFound) {
                // Do nothing
            }
        }
    }

    /**
     * Returns the description of the commands.
     *
     * @return The description.
     */
    @Override
    public String description() {
        return "Closes the current task. There should be an active commands before.";
    }
}
