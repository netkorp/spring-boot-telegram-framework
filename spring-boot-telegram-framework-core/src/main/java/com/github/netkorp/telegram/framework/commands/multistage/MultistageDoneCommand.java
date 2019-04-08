package com.github.netkorp.telegram.framework.commands.multistage;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.DoneCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Indicates the active command is done. It invokes the {@link MultistageCommand#done(Update)} of the active command.
 *
 * @see MultistageCommand
 * @see DoneCommand
 */
@TelegramCommand(name = "done", group = "Multistage")
@ConditionalOnBean(MultistageCommand.class)
@ConditionalOnSingleCandidate(DoneCommand.class)
public class MultistageDoneCommand extends AbstractSimpleCommand implements DoneCommand {

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     */
    @Override
    public void execute(Update update) {
        final Long idChat = update.getMessage().getChatId();

        try {
            if (commandManager.getActiveCommand(idChat).done(update)) {
                commandManager.removeActiveCommand(idChat);
            }
        } catch (CommandNotActive commandNotActive) {
            bot.sendMessage(commandNotActive.getMessage(), idChat);
            commandManager.getHelpCommand().ifPresent(command -> command.execute(update));
        }
    }

    /**
     * Returns the command's description, used to be displayed in help message.
     *
     * @return the command's description.
     */
    @Override
    public String description() {
        return "Marks as finished the current task. There should be an active commands before.";
    }
}
