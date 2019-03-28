package com.github.netkorp.telegram.framework.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class SecurityManager {

    private final List<Long> authorizedChats;

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

    public boolean isAuthorized(Long chatId) {
        return this.authorizedChats.contains(chatId);
    }
}
