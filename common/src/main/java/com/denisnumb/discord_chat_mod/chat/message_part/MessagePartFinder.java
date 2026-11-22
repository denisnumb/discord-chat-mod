package com.denisnumb.discord_chat_mod.chat.message_part;

import java.util.List;


@FunctionalInterface
public interface MessagePartFinder {
    List<Match> find(String message);

    record Match(int start, int end, MessagePart part) {}
}
