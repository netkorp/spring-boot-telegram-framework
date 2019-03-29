package com.github.netkorp.telegram.framework.bots;

import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import com.github.netkorp.telegram.framework.managers.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.invoke.MethodHandles;

@Component
public class PollingTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String botUsername;
    private String botToken;

    private final SecurityManager securityManager;
    private CommandManager commandManager;

    @Autowired
    public PollingTelegramBot(@Value("${telegram.bots.username}") String botUsername,
                              @Value("${telegram.bots.token}") String botToken,
                              SecurityManager securityManager) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.securityManager = securityManager;
    }

    @Autowired
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * This method is called when receiving updates via GetUpdates method
     *
     * @param update Update received
     */
    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage()) {
            Long idChat = update.getMessage().getChatId();

            try {
                // Checking if this is a commands
                if (update.getMessage().hasText()) {
                    String command = update.getMessage().getText().toLowerCase();

                    // Checking if it's a free command
                    try {
                        commandManager.getFreeCommand(command).execute(update);
                        return;
                    } catch (CommandNotFound commandNotFound) {
                        // Do nothing
                    }
                }

                // Checking if the chat is authorized
                if (securityManager.isAuthorized(idChat)) {
                    processMessage(update);
                }
            } catch (CommandNotFound commandNotFound) {
                sendMessage(commandNotFound.getMessage(), idChat);
                try {
                    commandManager.getHelpCommand().execute(update);
                } catch (CommandNotFound commandNotFound1) {
                    // Do nothing
                }
            }
        }
    }

    /**
     * Return bot username of this bot
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Returns the token of the bot to be able to perform Telegram Api Requests
     *
     * @return Token of the bot
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Process a given message.
     *
     * @param update The message sent by the user.
     */
    private void processMessage(Update update) throws CommandNotFound {
        final Long idChat = update.getMessage().getChatId();

        // Perhaps is a commands
        if (update.getMessage().hasText()) {
            String commandText = update.getMessage().getText().toLowerCase();

            if (reservedCommands(commandText, update)) {
                return;
            }

            // Trying to get commands
            try {
                Command command = commandManager.getCommand(commandText);

                // If there is no active commands defined, we understand this is an attempt to define/execute one
                if (!commandManager.hasActiveCommand(idChat)) {

                    if (command instanceof MultistageCommand) {
                        if (((MultistageCommand) command).init(update)) {
                            commandManager.setActiveCommand(idChat, ((MultistageCommand) command));
                        }
                    } else {
                        command.execute(update);
                    }

                    return;
                }
            } catch (CommandNotFound commandNotFound) {
                // Perhaps we're talking about data here. If there is an active commands, we leave it to it.
                if (!commandManager.hasActiveCommand(idChat)) {
                    throw commandNotFound;
                }
            }
        }

        // If there is a commands active, we handle the messages as data
        try {
            commandManager.getActiveCommand(idChat).execute(update);
        } catch (CommandNotActive commandNotActive) {
            sendMessage(commandNotActive.getMessage(), idChat);
        }
    }

    /**
     * Checks if the entered text matches with some reserved command.
     *
     * @param commandText Text entered by the user.
     * @param update The message sent by the user.
     * @return If some reserved command was executed or not.
     */
    private boolean reservedCommands(String commandText, Update update) {
        try {
            if (commandManager.getCloseCommand().getName().equals(commandText)) {
                commandManager.getCloseCommand().execute(update);
                return true;
            }

            if (commandManager.getDoneCommand().getName().equals(commandText)) {
                commandManager.getDoneCommand().execute(update);
                return true;
            }
        } catch (CommandNotFound ignored) {
        }

        return false;
    }

    /**
     * @param content Message content.
     * @param idChat  Chat's ID.
     */
    public void sendMessage(String content, Long idChat) {
        sendMessage(content, idChat, false);
    }

    /**
     * @param content Message content.
     * @param idChat  Chat's ID.
     * @param html    If HTML format is enabled or not.
     */
    public void sendMessage(String content, Long idChat, boolean html) {
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(idChat).setText(content).enableHtml(html);

        try {
            this.execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
