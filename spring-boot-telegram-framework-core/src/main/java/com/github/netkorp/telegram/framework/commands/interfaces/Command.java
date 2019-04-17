package com.github.netkorp.telegram.framework.commands.interfaces;

/**
 * Contains the logic of a command to be executed by the users.
 */
public interface Command {

    /**
     * Returns the command's description key, used to retrieve the help message.
     *
     * @return the command's description key.
     */
    default String descriptionKey() {
        String className = this.getClass().getSimpleName().toLowerCase();
        if (!"command".equals(className) && className.endsWith("command")) {
            className = className.substring(0, className.length() - 7);
        }

        return "commands.description." + className;
    }
}
