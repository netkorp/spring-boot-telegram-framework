package com.github.netkorp.telegram.framework.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Contains the configuration of locale for the bot.
 */
@Configuration
public class LocaleConfiguration {

    /**
     * Returns a custom {@link MessageSource} for resolving the Telegram Framework messages.
     *
     * @return the custom {@link MessageSource} instance.
     */
    @Bean("TelegramFrameworkMessageSource")
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
