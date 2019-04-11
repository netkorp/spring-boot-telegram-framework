package com.github.netkorp.telegram.framework.bots;

import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.SimpleCommand;
import com.github.netkorp.telegram.framework.commands.multistage.MultistageCloseCommand;
import com.github.netkorp.telegram.framework.commands.multistage.MultistageDoneCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import com.github.netkorp.telegram.framework.managers.CommandManager;
import com.github.netkorp.telegram.framework.managers.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.invoke.MethodHandles;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Provides the component for sharing information with Telegram using
 * <a href="https://core.telegram.org/bots/api#getupdates">long-polling</a> method.
 * It has the responsibility to execute the proper command when an incoming message is received.
 */
@Component
public class PollingTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * The bot's username.
     */
    private String botUsername;

    /**
     * The bot's token.
     */
    private String botToken;

    /**
     * The component to know which user is authorized.
     */
    private final SecurityManager securityManager;

    /**
     * The component for managing all of the available commands in the bot.
     */
    private final CommandManager commandManager;

    /**
     * Constructs a new {@link PollingTelegramBot} instance with both username and token of the bot,
     * the {@link SecurityManager} component instance and the {@link CommandManager} instance.
     *
     * @param botUsername     the username of the bot.
     * @param botToken        the token of the bot.
     * @param securityManager the {@link SecurityManager} component instance.
     * @param commandManager  the {@link CommandManager} instance.
     */
    @Autowired
    public PollingTelegramBot(@Value("${telegram.bots.username}") String botUsername,
                              @Value("${telegram.bots.token}") String botToken,
                              SecurityManager securityManager,
                              @Lazy CommandManager commandManager) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.securityManager = securityManager;
        this.commandManager = commandManager;
    }

    /**
     * This method is called when receiving updates via GetUpdates method.
     *
     * @param update Update received.
     */
    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage()) {
            Long idChat = update.getMessage().getChatId();

            try {
                // Checking if this is a command
                if (update.getMessage().hasText()) {
                    Map.Entry<String, String[]> command = getCommand(update);

                    // Checking if it's a non-secure command
                    try {
                        Command commandInstance = commandManager.getNonSecureCommand(command.getKey());
                        if (commandInstance instanceof SimpleCommand) {
                            ((SimpleCommand) commandInstance).execute(update, command.getValue());
                        }
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
                commandManager.getHelpCommand().ifPresent(command -> command.execute(update));
            }
        }
    }

    /**
     * Returns the command invoked by the user from the message sent by him, cleaning the text and deleting the bot's username.
     *
     * @param update the received update.
     * @return the command and the parameters.
     */
    private Map.Entry<String, String[]> getCommand(Update update) {
        String cleanedCommand = update.getMessage().getText().toLowerCase()
                .replace(String.format("@%s", getBotUsername().toLowerCase()), "");

        String[] dividedText = cleanedCommand.split(" ");

        return new AbstractMap.SimpleEntry<>(dividedText[0],
                Arrays.copyOfRange(dividedText, 1, dividedText.length));
    }

    /**
     * Returns the bot's username.
     *
     * @return the bot's username.
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Returns the bot's token.
     *
     * @return the bot's token.
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Processes a given message.
     *
     * @param update the message sent by the user.
     */
    private void processMessage(Update update) throws CommandNotFound {
        final Long idChat = update.getMessage().getChatId();

        // Perhaps is a command
        if (update.getMessage().hasText()) {
            Map.Entry<String, String[]> commandText = getCommand(update);

            if (reservedCommands(commandText.getKey(), update)) {
                return;
            }

            // Trying to get a command
            try {
                Command command = commandManager.getCommand(commandText.getKey());

                // If there is no active command defined, we understand this is an attempt to define/execute one
                if (!commandManager.hasActiveCommand(idChat)) {

                    if (command instanceof MultistageCommand) {
                        if (((MultistageCommand) command).init(update)) {
                            commandManager.setActiveCommand(idChat, ((MultistageCommand) command));
                        }
                    } else if (command instanceof SimpleCommand) {
                        ((SimpleCommand) command).execute(update, commandText.getValue());
                    }

                    return;
                }
            } catch (CommandNotFound commandNotFound) {
                // Perhaps we're talking about data here. If there is an active command, we leave it to it.
                if (!commandManager.hasActiveCommand(idChat)) {
                    throw commandNotFound;
                }
            }
        }

        // If there is an active command, we handle the messages as data
        try {
            commandManager.getActiveCommand(idChat).execute(update);
        } catch (CommandNotActive commandNotActive) {
            sendMessage(commandNotActive.getMessage(), idChat);
        }
    }

    /**
     * Checks if the entered text matches with some reserved command.
     * If this is the case it will execute the corresponding command.
     *
     * @param commandText the text entered by the user.
     * @param update      the message sent by the user.
     * @return {@code true} if some reserved command was executed; {@code false} otherwise.
     */
    private boolean reservedCommands(String commandText, Update update) {
        Optional<MultistageCloseCommand> closeCommand = commandManager.getCloseCommand()
                .filter(command -> CommandManager.getCommand(command).equals(commandText));

        if (closeCommand.isPresent()) {
            closeCommand.get().execute(update);
            return true;
        }

        Optional<MultistageDoneCommand> doneCommand = commandManager.getDoneCommand()
                .filter(command -> CommandManager.getCommand(command).equals(commandText));

        if (doneCommand.isPresent()) {
            doneCommand.get().execute(update);
            return true;
        }

        return false;
    }

    /**
     * Sends a text message to Telegram.
     * This is a shortcut for {@link #sendMessage(String, Long, boolean)} with HTML format disabled.
     *
     * @param content the message content.
     * @param idChat  the chat identification to which the message should be sent.
     */
    public void sendMessage(String content, Long idChat) {
        sendMessage(content, idChat, false);
    }

    /**
     * Sends a text message to Telegram. This is a shortcut for {@link #execute(BotApiMethod)}.
     *
     * @param content the message content.
     * @param idChat  the chat identification to which the message should be sent.
     * @param html    {@code true} if HTML format is enabled or {@code false} otherwise.
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
