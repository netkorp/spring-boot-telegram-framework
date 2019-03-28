package com.github.netkorp.telegram.framework;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@PropertySource({"classpath:spring-boot-telegram-framework.properties"})
public class SpringBootTelegramFrameworkConfiguration {
}
