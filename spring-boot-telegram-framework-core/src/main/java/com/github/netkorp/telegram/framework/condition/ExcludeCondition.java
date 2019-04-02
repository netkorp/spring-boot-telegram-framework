package com.github.netkorp.telegram.framework.condition;

import com.github.netkorp.telegram.framework.annotations.TelegramCommand;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class ExcludeCondition implements Condition {

    /**
     * Determine if the condition matches.
     *
     * @param context  the condition context
     * @param metadata metadata of the {@link AnnotationMetadata class}
     *                 or {@link MethodMetadata method} being checked
     * @return {@code true} if the condition matches and the component can be registered,
     * or {@code false} to veto the annotated component's registration
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String excludeValue = context.getEnvironment().getProperty("telegram.commands.exclude");
        if (excludeValue != null && context.getBeanFactory() != null) {
            Map<String, Object> attributes = metadata.getAnnotationAttributes(TelegramCommand.class.getName());
            if (attributes != null) {
                String commandName = (String) attributes.get("name");
                return !Arrays.asList(excludeValue.split(",")).contains(commandName);
            }
        }

        return true;
    }
}
