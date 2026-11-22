package com.denisnumb.discord_chat_mod.chat.message_part;

import com.denisnumb.discord_chat_mod.chat.CommonMessageFormatter.FormattedMessage;

public interface MessagePart {
    FormattedMessage format(MessageFormatContext context);
}
