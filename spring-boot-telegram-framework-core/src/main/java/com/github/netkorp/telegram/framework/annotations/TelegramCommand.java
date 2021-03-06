package com.github.netkorp.telegram.framework.annotations;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated element represents a command that could be executed by the user.
 * It should be used in classes that implement the
 * {@link com.github.netkorp.telegram.framework.commands.interfaces.Command} interface.
 *
 * @see com.github.netkorp.telegram.framework.commands.abstracts.AbstractSimpleCommand
 * @see com.github.netkorp.telegram.framework.commands.abstracts.AbstractMultistageCommand
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
@Component
public @interface TelegramCommand {

    /**
     * Returns the names of the command that will be used to invoke it.
     *
     * @return the names of the command.
     */
    String[] name();

    /**
     * Indicates the group of commands where the command will be included, if any; empty String otherwise.
     *
     * @return the name of the group.
     */
    String group() default "";

    /**
     * Returns the description of the command.
     *
     * @return the description of the command.
     */
    String description() default "";

    /**
     * Returns {@code true} if the command is secure.
     *
     * @return {@code true} if the command is secure; {@code false} otherwise. By default the command is secure.
     */
    boolean secure() default true;
}