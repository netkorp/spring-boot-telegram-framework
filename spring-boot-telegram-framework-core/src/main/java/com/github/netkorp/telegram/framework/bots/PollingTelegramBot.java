package com.github.netkorp.telegram.framework.bots;

import com.github.netkorp.telegram.framework.commands.interfaces.Command;
import com.github.netkorp.telegram.framework.commands.interfaces.MultistageCommand;
import com.github.netkorp.telegram.framework.commands.interfaces.SimpleCommand;
import com.github.netkorp.telegram.framework.commands.multistage.MultistageCloseCommand;
import com.github.netkorp.telegram.framework.commands.multistage.MultistageDoneCommand;
import com.github.netkorp.telegram.framework.exceptions.CommandNotActive;
import com.github.netkorp.telegram.framework.exceptions.CommandNotFound;
import com.github.netkorp.telegram.framework.exceptions.UserNotAuthorized;
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
            Long chatId = update.getMessage().getChatId();

            if (commandManager.hasActiveCommand(chatId)) {
                if (!update.getMessage().isCommand()
                        || !reservedCommands(getCleanedCommand(update), update)) {
                    try {
                        commandManager.getActiveCommand(chatId).execute(update);
                    } catch (CommandNotActive commandNotActive) {
                        // Do nothing. This point is impossible to reach.
                    }
                }

                return;
            }

            // Checking if this is a command
            if (update.getMessage().isCommand()) {
                try {
                    Map.Entry<String, String[]> commandAndArgs = getCleanedCommandAndArgs(update);
                    Command command = commandManager.getCommand(commandAndArgs.getKey());

                    if (!securityManager.isAuthorized(chatId, command)) {
                        throw new UserNotAuthorized();
                    }

                    if (command instanceof MultistageCommand && ((MultistageCommand) command).init(update)) {
                        commandManager.setActiveCommand(chatId, ((MultistageCommand) command));
                    } else if (command instanceof SimpleCommand) {
                        if (commandAndArgs.getValue().length == 0) {
                            ((SimpleCommand) command).execute(update);
                        } else {
                            ((SimpleCommand) command).execute(update, commandAndArgs.getValue());
                        }
                    }
                } catch (CommandNotFound commandNotFound) {
                    sendMessage(commandNotFound.getMessage(), chatId);
                    commandManager.getHelpCommand()
                            .filter(command -> securityManager.isAuthorized(chatId, command))
                            .ifPresent(command -> command.execute(update));
                } catch (UserNotAuthorized userNotAuthorized) {
                    sendMessage(userNotAuthorized.getMessage(), chatId);
                }
            } else {
                sendMessage("That is not a command", chatId);
            }
        }
    }

    /**
     * Returns the command invoked by the user and the parameters, cleaning the text and deleting the bot's username.
     *
     * @param update the received update.
     * @return the command and the parameters.
     */
    private Map.Entry<String, String[]> getCleanedCommandAndArgs(Update update) {
        String cleanedCommand = getCleanedCommand(update);

        String[] dividedText = cleanedCommand.split(" ");

        return new AbstractMap.SimpleEntry<>(dividedText[0],
                Arrays.copyOfRange(dividedText, 1, dividedText.length));
    }

    /**
     * Returns the command invoked by the user, cleaning the text and deleting the bot's username.
     *
     * @param update the received update.
     * @return the command.
     */
    private String getCleanedCommand(Update update) {
        return update.getMessage().getText().toLowerCase()
                .replace(String.format("@%s", getBotUsername().toLowerCase()), "");
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
     * Checks if the entered text matches with some reserved command.
     * If this is the case it will execute the corresponding command.
     *
     * @param commandText the text entered by the user.
     * @param update      the message sent by the user.
     * @return {@code true} if some reserved command was executed; {@code false} otherwise.
     */
    private boolean reservedCommands(String commandText, Update update) {
        Optional<MultistageCloseCommand> closeCommand = commandManager.getCloseCommand()
                .filter(command -> CommandManager.getCommandFullName(command).equals(commandText));

        if (closeCommand.isPresent()) {
            closeCommand.get().execute(update);
            return true;
        }

        Optional<MultistageDoneCommand> doneCommand = commandManager.getDoneCommand()
                .filter(command -> CommandManager.getCommandFullName(command).equals(commandText));

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
