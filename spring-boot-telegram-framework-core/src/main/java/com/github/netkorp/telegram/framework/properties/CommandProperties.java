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
     * List with the names of non-secure commands.
     */
    private List<String> nonSecure;

    /**
     * Returns the list with the names of non-secure commands.
     *
     * @return the list with the names of non-secure commands.
     */
    public List<String> getNonSecure() {
        return nonSecure;
    }

    /**
     * Sets the names of non-secure commands.
     *
     * @param nonSecure the names of non-secure commands.
     */
    public void setNonSecure(List<String> nonSecure) {
        this.nonSecure = nonSecure;
    }
}
