package com.denisnumb.discord_chat_mod.linking;

import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;

public interface AccountLinkListener {
    default void onLinked(AccountLink link) {}

    default void onUnlinked(AccountLink link, UnlinkReason reason) {}
}
