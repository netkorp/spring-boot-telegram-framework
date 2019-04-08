package com.github.netkorp.telegram.framework.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Contains the properties associated to the commands.
 */
@Component
@ConfigurationProperties("telegram.commands")
public class CommandProperties {

    /**
     * List with the names of free commands.
     */
    private List<String> free;

    /**
     * Returns the list with the names of free commands.
     *
     * @return the list with the names of free commands.
     */
    public List<String> getFree() {
        return free;
    }

    /**
     * Sets the names of free commands.
     *
     * @param free the names of free commands.
     */
    public void setFree(List<String> free) {
        this.free = free;
    }
}
