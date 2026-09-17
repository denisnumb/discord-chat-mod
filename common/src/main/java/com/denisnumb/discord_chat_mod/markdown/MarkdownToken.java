package com.denisnumb.discord_chat_mod.markdown;

import com.denisnumb.discord_chat_mod.utils.ColorUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.utils.ColorUtils.getHexColor;

public final class MarkdownToken {
    public String rawText;
    public String text;
    public String url = null;
    public boolean bold = false;
    public boolean italic = false;
    public boolean underlined = false;
    public boolean strikethrough = false;
    public boolean obfuscated = false;
    public boolean isMention = false;
    public Integer color = null;
    public int[] gradientColors = null;
    public boolean isSpecialCharacters = false;

    public MarkdownToken(String rawText){
        this(rawText, rawText);
    }

    public MarkdownToken(String rawText, String text){
        this.rawText = rawText;
        this.text = text;
    }

    public boolean isUrl(){
        return url != null && !url.isEmpty();
    }

    public boolean isColored() {
        return color != null;
    }

    public boolean isGradient(){
        return gradientColors != null && gradientColors.length > 0;
    }

    public boolean hasMarkdown(){
        return (isUrl()
                || bold
                || italic
                || underlined
                || strikethrough
                || obfuscated
                || isMention
                || isColored()
                || isGradient()
                || isSpecialCharacters
        );
    }

    public void combineStyles(MarkdownToken another){
        if (isSpecialCharacters)
            return;

        url = isUrl() ? url : another.url;
        bold |= another.bold;
        italic |= another.italic;
        underlined |= another.underlined;
        strikethrough |= another.strikethrough;
        obfuscated |= another.obfuscated;

        if (!isColored() && !isGradient()) {
            color = another.color;
            gradientColors = another.gradientColors;
        }
    }

    public String toString(){
        if (isSpecialCharacters)
            return rawText;

        StringBuilder result = new StringBuilder("[");

        result.append(String.format("rawText=\"%s\", text=\"%s\"", rawText, text));
        if (isUrl()) result.append(String.format(", url=\"%s\"", url));
        if (bold) result.append(", bold");
        if (italic) result.append(", italic");
        if (underlined) result.append(", underlined");
        if (strikethrough) result.append(", strikethrough");
        if (obfuscated) result.append(", obfuscated");
        if (isMention) result.append(", isMention");
        if (isColored()) result.append(String.format(", color=\"%s\"", getHexColor(color)));
        if (isGradient()) result.append(String.format(", gradient=[%s]",
                Arrays.stream(gradientColors)
                        .mapToObj(ColorUtils::getHexColor)
                        .collect(Collectors.joining(", "))
        ));
        result.append("]");

        return result.toString();
    }
}
