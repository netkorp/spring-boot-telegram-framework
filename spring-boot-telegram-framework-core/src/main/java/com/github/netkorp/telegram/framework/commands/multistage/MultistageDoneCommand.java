package com.github.netkorp.telegram.framework.commands.multistage;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.managers.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Indicates the active command is done. It invokes the {@link MultistageCommand#done(Update)} of the active command.
 *
 * @see MultistageCloseCommand
 */
@TelegramCommand(name = "done", group = "commands.groups.done", description = "commands.description.done", secure = false)
@ConditionalOnBean(MultistageCommand.class)
public class MultistageDoneCommand extends AbstractSimpleCommand {

    /**
     * The component to know which user is authorized.
     */
    private final SecurityManager securityManager;

    /**
     * Constructs a new {@link MultistageDoneCommand} instance with the {@link SecurityManager} component instance.
     *
     * @param securityManager the {@link SecurityManager} component instance.
     */
    @Autowired
    public MultistageDoneCommand(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     * @param args   the parameters passed to the command execution.
     */
    @Override
    public void execute(Update update, String[] args) {
        final Long chatId = update.getMessage().getChatId();

        try {
            if (commandManager.getActiveCommand(chatId).done(update)) {
                commandManager.removeActiveCommand(chatId);
            }
        } catch (CommandNotActive commandNotActive) {
            bot.sendMessage(commandNotActive.getMessage(), chatId);
            commandManager.getHelpCommand()
                    .filter(command -> securityManager.isAuthorized(chatId, command))
                    .ifPresent(command -> command.execute(update));
        }
    }
}
