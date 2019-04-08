package com.github.netkorp.telegram.framework.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Indicates the command is the command to invoke {@link MultistageCommand#done(Update)} of the active command.
 */
public interface DoneCommand extends Command {
}
