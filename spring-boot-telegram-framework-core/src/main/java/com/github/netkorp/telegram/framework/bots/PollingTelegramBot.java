package com.github.netkorp.telegram.framework.bots;

import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.exceptions.*;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import com.github.netkorp.telegram.framework.managers.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Component
public class PollingTelegramBot extends TelegramLongPollingBot {

    public static final String HELP_COMMAND_NAME = "help";
    public static final String DONE_COMMAND_NAME = "done";
    public static final String EXIT_COMMAND_NAME = "close";

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

                    // Command ID is the unique free commands
                    if ("id".equals(command)) {
                        commandManager.getCommand(command).execute(update);
                    }

                    // For the rest of commands we have to check if the chat is authorized
                    if (securityManager.isAuthorized(idChat)) {
                        switch (command) {
                            case HELP_COMMAND_NAME:
                                commandManager.getCommand(command).execute(update);
                                break;
                            case "id":
                                break;
                            default:
                                processMessage(update);
                                break;
                        }
                    }

                    return;
                }

                // Checking if the chat is authorized
                if (securityManager.isAuthorized(idChat)) {
                    processMessage(update);
                }
            } catch (CommandNotFound commandNotFound) {
                sendMessage(commandNotFound.getMessage(), idChat);
                try {
                    commandManager.getCommand(HELP_COMMAND_NAME).execute(update);
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

            // Commands exit and done are basic commands (reserved by the system)
            if (EXIT_COMMAND_NAME.equals(commandText) || DONE_COMMAND_NAME
                    .equals(commandText)) {
                try {
                    commandManager.getCommand(commandText).execute(update);
                } catch (CommandNotFound commandNotFound) {
                    // Do nothing
                }
                return;
            }

            // Trying to get a commands
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
            e.printStackTrace();
        }
    }
}
