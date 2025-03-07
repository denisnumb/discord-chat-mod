package com.denisnumb.discord_chat_mod.advancement;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdvancementDisplayComponent {
    @SerializedName("text")
    public String text;
    @SerializedName("translate")
    public String translate;
    @SerializedName("bold")
    public boolean bold;
    @SerializedName("italic")
    public boolean italic;
    @SerializedName("strikethrough")
    public boolean strikethrough;
    @SerializedName("underlined")
    public boolean underlined;
    @SerializedName("obfuscated")
    public boolean obfuscated;
    @SerializedName("with")
    public List<AdvancementDisplayComponent> with;

    public String applyStyles(String translatedText){
        if (bold) translatedText = "**" + translatedText + "**";
        if (italic) translatedText = "*" + translatedText + "*";
        if (strikethrough) translatedText = "~~" + translatedText + "~~";
        if (underlined) translatedText = "__" + translatedText + "__";
        if (obfuscated) translatedText = "||" + translatedText + "||";

        return translatedText;
    }
}
