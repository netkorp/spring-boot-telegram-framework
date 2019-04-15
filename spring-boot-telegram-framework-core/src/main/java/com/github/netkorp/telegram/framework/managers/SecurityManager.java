package com.github.netkorp.telegram.framework.managers;

import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides the component for securing the bot by containing the list of authorized chat identifications.
 */
@Service
public class SecurityManager {

    /**
     * The list of authorized chat identification.
     */
    private final List<Long> authorizedChats;

    /**
     * The component for managing all of the available commands in the bot.
     */
    private final CommandManager commandManager;

    /**
     * Constructs a new {@link SecurityManager} instance with the list of the authorized chat identifications.
     *
     * @param authorizedChats the list of the authorized chat identifications.
     */
    @Autowired
    public SecurityManager(@Value("${telegram.authorized.idChat}") String authorizedChats, @Lazy CommandManager commandManager) {
        this.authorizedChats = new LinkedList<>();
        this.commandManager = commandManager;

        for (String chatID : authorizedChats.split(",")) {
            try {
                this.authorizedChats.add(Long.parseLong(chatID));
            } catch (Exception ex) {
                // Do nothing
            }
        }
    }

    /**
     * Returns {@code true} if the chat identification is authorized.
     *
     * @param chatId the chat identification of the person from whom you want to know if it is authorized.
     * @return {@code true} if the chat identification is authorized; {@code false} otherwise.
     */
    public boolean isAuthorized(Long chatId) {
        return this.authorizedChats.contains(chatId);
    }

    /**
     * Returns {@code true} if the chat identification is authorized to invoke the command.
     *
     * @param chatId  the chat identification of the person from whom you want to know if it is authorized to invoke the command.
     * @param command the command to be invoked.
     * @return {@code true} if the chat identification is authorized to invoke the command; {@code false} otherwise.
     */
    public boolean isAuthorized(Long chatId, Command command) {
        if (commandManager.isNonSecureCommand(command)) {
            return true;
        }

        return this.authorizedChats.contains(chatId);
    }
}
