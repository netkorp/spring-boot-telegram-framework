package com.github.netkorp.multistagecommand.commands;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import com.github.netkorp.telegram.framework.commands.abstracts.AbstractMultistageCommand;
import org.apache.logging.log4j.util.Strings;
import org.telegram.telegrambots.meta.api.objects.Update;

@TelegramCommand(name = "data")
public class DataCommand extends AbstractMultistageCommand {

    /**
     * Initializes a commands.
     *
     * @param update The received message.
     */
    @Override
    public boolean init(Update update) {
        bot.sendMessage("I cant tell you your <b>first_name</b> and your <b>last_name</b>",
                update.getMessage().getChatId(), true);
        return true;
    }

    /**
     * Indicates a commands is done.
     *
     * @param update The received message.
     */
    @Override
    public boolean done(Update update) {
        bot.sendMessage("Great!!!", update.getMessage().getChatId());
        return true;
    }

    /**
     * Closes a commands.
     *
     * @param update The received message.
     */
    @Override
    public boolean close(Update update) {
        bot.sendMessage("Great!!! All is good, I didn't store any data anyway.", update.getMessage().getChatId());
        return true;
    }

    /**
     * Processes the data of the commands.
     *
     * @param update The received message.
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
                bot.sendMessage("I don't recognize that order", update.getMessage().getChatId());
                break;
        }
    }

    /**
     * Returns the description of the commands.
     *
     * @return The description.
     */
    @Override
    public String description() {
        return "It shows you your first name and last name";
    }
}
