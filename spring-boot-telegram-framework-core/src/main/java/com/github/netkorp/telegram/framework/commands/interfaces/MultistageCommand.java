package com.github.netkorp.telegram.framework.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MultistageCommand extends Command {

    /**
     * Initializes a commands.
     *
     * @param update The received message.
     */
    boolean init(final Update update);

    /**
     * Indicates a commands is done.
     *
     * @param update The received message.
     */
    boolean done(final Update update);

    /**
     * Closes a commands.
     *
     * @param update The received message.
     */
    boolean close(final Update update);
}
