package com.github.netkorp.multistagecommand.commands;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractMultistageCommand;
import org.apache.logging.log4j.util.Strings;
import org.telegram.telegrambots.meta.api.objects.Update;

@TelegramCommand(name = "data")
public class DataCommand extends AbstractMultistageCommand {

    /**
     * Initializes the command. It's invoked where the command name matches with the text entered by the user.
     * It returns {@code true} if the initialization was successful.
     * In this case the command will be established as active command.
     *
     * @param update the received message.
     * @return {@code true} if the initialization was successful; {@code false} otherwise.
     */
    @Override
    public boolean init(Update update) {
        bot.sendMessage("I cant tell you your <b>first_name</b> and your <b>last_name</b>",
                update.getMessage().getChatId(), true);
        return true;
    }

    /**
     * Indicates a command is done. It's invoked where the command
     * {@link com.github.netkorp.telegram.framework.commands.multistage.MultistageDoneCommand} is invoked.
     * It returns {@code true} if everything was fine during the process. In this case the command will be removed as active command.
     *
     * @param update the received message.
     * @return {@code true} if everything was fine during the process; {@code false} otherwise.
     * @see com.github.netkorp.telegram.framework.commands.multistage.MultistageDoneCommand
     */
    @Override
    public boolean done(Update update) {
        bot.sendMessage("Great!!!", update.getMessage().getChatId());
        return true;
    }

    /**
     * Closes a command. It's invoked where the command
     * {@link com.github.netkorp.telegram.framework.commands.multistage.MultistageCloseCommand} is invoked.
     * It returns {@code true} if everything was fine during the process. In this case the command will be removed as active command.
     *
     * @param update the received message.
     * @return {@code true} if everything was fine during the process; {@code false} otherwise.
     * @see com.github.netkorp.telegram.framework.commands.multistage.MultistageCloseCommand
     */
    @Override
    public boolean close(Update update) {
        bot.sendMessage("Great!!! All is good, I didn't store any data anyway.", update.getMessage().getChatId());
        return true;
    }

    /**
     * Processes the data sent by the users.
     *
     * @param update the received message.
     */
    @Override
    public void execute(Update update) {
        switch (update.getMessage().getText()) {
            case "first_name":
                String firstName = update.getMessage().getChat().getFirstName();
                bot.sendMessage(Strings.isEmpty(firstName) ? "Sorry, I can't find your first name" : firstName, update.getMessage().getChatId());
                break;
            case "last_name":
                String lastName = update.getMessage().getChat().getLastName();
                bot.sendMessage(Strings.isEmpty(lastName) ? "Sorry, I can't find your last name" : lastName, update.getMessage().getChatId());
                break;
            default:
                bot.sendMessage("I don't recognize that command", update.getMessage().getChatId());
                break;
        }
    }

    /**
     * Returns the command's description, used to be displayed in help message.
     *
     * @return the command's description.
     */
    @Override
    public String description() {
        return "It shows you your first name and last name";
    }
}
