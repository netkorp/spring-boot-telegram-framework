package com.github.netkorp.overridebasiccommand.commands;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.HelpCommand;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import org.springframework.context.i18n.LocaleContextHolder;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.StringJoiner;

@TelegramCommand(name = "assistance", secure = false)
public class OwnHelpCommand extends AbstractSimpleCommand implements HelpCommand {

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     * @param args   the parameters passed to the command execution.
     */
    @Override
    public void execute(Update update, String[] args) {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        stringJoiner.add(String.format("%s:", messageSource.getMessage("commands.ownhelp.title", null,
                LocaleContextHolder.getLocale())));
        commandManager.getAvailableNonSecureCommands()
                .forEach(command -> stringJoiner.add(String.format("%s - <b>%s</b>", CommandManager.getCommandFullNames(command),
                        messageSource.getMessage(command.getClass().getAnnotation(TelegramCommand.class).description(),
                                null, LocaleContextHolder.getLocale()))));
        bot.sendMessage(stringJoiner.toString(), update.getMessage().getChatId(), true);
    }
}
