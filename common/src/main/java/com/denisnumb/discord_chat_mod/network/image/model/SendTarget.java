package com.denisnumb.discord_chat_mod.network.image.model;

import java.util.List;

public interface SendTarget {
    static SendTarget all() {
        return new All();
    }
    
    static SendTarget players(List<String> nicknames) {
        return new Players(nicknames);
    }
    
    static SendTarget team(String teamName) {
        return new Team(teamName);
    }

    record All() implements SendTarget {}
    record Players(List<String> nicknames) implements SendTarget {}
    record Team(String teamName) implements SendTarget {}
}