package com.github.netkorp.telegram.framework.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Contains the logic of each step in the multistage command flow.
 */
public interface MultistageCommand extends Command {

    /**
     * Initializes the command. It's invoked where the command name matches with the text entered by the user.
     * It returns {@code true} if the initialization was successful.
     * In this case the command will be established as active command.
     *
     * @param update the received message.
     * @return {@code true} if the initialization was successful; {@code false} otherwise.
     */
    boolean init(final Update update);

    /**
     * Indicates a command is done. It's invoked where the command
     * {@link com.github.netkorp.telegram.framework.commands.multistage.MultistageDoneCommand} is invoked.
     * It returns {@code true} if everything was fine during the process. In this case the command will be removed as active command.
     *
     * @param update the received message.
     * @return {@code true} if everything was fine during the process; {@code false} otherwise.
     * @see com.github.netkorp.telegram.framework.commands.multistage.MultistageDoneCommand
     */
    boolean done(final Update update);

    /**
     * Closes a command. It's invoked where the command
     * {@link com.github.netkorp.telegram.framework.commands.multistage.MultistageCloseCommand} is invoked.
     * It returns {@code true} if everything was fine during the process. In this case the command will be removed as active command.
     *
     * @param update the received message.
     * @return {@code true} if everything was fine during the process; {@code false} otherwise.
     * @see com.github.netkorp.telegram.framework.commands.multistage.MultistageCloseCommand
     */
    boolean close(final Update update);
}
