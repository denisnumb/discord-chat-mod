package com.denisnumb.discord_chat_mod.discord.chat.template;

import com.denisnumb.discord_chat_mod.chat.template.TemplatePlaceholder;

import java.util.Map;

public record StringTemplate<T extends TemplateType>(String rawTemplate) {
    public String applyParameters(TemplateType.ParamsBuilder<T> paramsBuilder) {
        String result = rawTemplate;
        for (Map.Entry<TemplatePlaceholder, String> param : paramsBuilder.build().entrySet())
            result = result.replace(param.getKey().getPlaceholder(), param.getValue());

        return result;
    }
}
