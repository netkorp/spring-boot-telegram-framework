package com.github.netkorp.telegram.framework.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
     * Constructs a new {@link SecurityManager} instance with the list of the authorized chat identifications.
     *
     * @param authorizedChats the list of the authorized chat identifications.
     */
    @Autowired
    public SecurityManager(@Value("${telegram.authorized.idChat}") String authorizedChats) {
        this.authorizedChats = new LinkedList<>();

        for (String chatID : authorizedChats.split(",")) {
            try {
                this.authorizedChats.add(Long.parseLong(chatID));
            } catch (Exception ex) {
                // Do nothing
            }
        }
    }

    /**
     * Returns <code>true</code> if the chat identification is authorized.
     *
     * @param chatId the chat identification of the person from whom you want to know if it is authorized.
     * @return <code>true</code> if the chat identification is authorized; <code>false</code> otherwise.
     */
    public boolean isAuthorized(Long chatId) {
        return this.authorizedChats.contains(chatId);
    }
}
