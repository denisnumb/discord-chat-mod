package com.denisnumb.discord_chat_mod.chat.template;

public sealed interface TemplatePlaceholder permits TemplateParameter, TemplateParameter.Translatable {
    String getPlaceholder();
}