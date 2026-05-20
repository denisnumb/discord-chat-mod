package com.denisnumb.discord_chat_mod.chat_style;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.IConfigProvider;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.chat_style.CustomChatTypeRegistry.*;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.COMMANDS_MESSAGE_DISPLAY_INCOMING;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.COMMANDS_MESSAGE_DISPLAY_OUTGOING;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.X;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.Y;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.Z;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.DIMENSION;

public class ChatStyleUtils {
    public static Map<String, Style> parseTemplateParameterStyles(MutableComponent template, String... parameters) {
        Map<String, Style> parameterStyles = new HashMap<>();

        String orPattern = Arrays.stream(parameters)
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        Pattern parameterPattern = Pattern.compile(orPattern);

        for (Component templatePart : template.toFlatList()) {
            String templateString = templatePart.getString();
            Matcher matcher = parameterPattern.matcher(templateString);

            if (!matcher.find())
                continue;

            do {
                parameterStyles.put(matcher.group(), templatePart.getStyle());
            } while (matcher.find());
        }

        return parameterStyles;
    }

    @Nullable
    public static String getConfigTemplateByChatType(ResourceKey<ChatType> key) {
        IConfigProvider config = ConfigProvider.getConfig();

        return switch (key.location().getPath()) {
            case CHAT_PATH -> config.minecraftPlayerMessageStyle();
            case SAY_COMMAND_PATH -> config.minecraftSayCommandStyle();
            case MSG_COMMAND_INCOMING_PATH -> setConfigTemplateTranslatableParameters(
                    config.minecraftTellMessageReceivedStyle(),
                    COMMANDS_MESSAGE_DISPLAY_INCOMING
            );
            case MSG_COMMAND_OUTGOING_PATH -> setConfigTemplateTranslatableParameters(
                    config.minecraftTellMessageSentStyle(),
                    COMMANDS_MESSAGE_DISPLAY_OUTGOING
            );
            case TEAM_MSG_COMMAND_INCOMING_PATH -> config.minecraftTeamMessageReceivedStyle();
            case TEAM_MSG_COMMAND_OUTGOING_PATH -> config.minecraftTeamMessageSentStyle();
            case EMOTE_COMMAND_PATH -> config.minecraftMeCommandStyle();
            default -> null;
        };
    }

    public static MutableComponent parseConfigTemplateMarkdown(String configTemplate) {
        return new MarkdownToComponentConverter(
                MarkdownParser.parseMarkdown(configTemplate)
        ).convertMarkdownTokensToComponent();
    }

    public static String setConfigTemplateTranslatableParameters(String configTemplate, String... translatableParameters) {
        String result = configTemplate;

        for (String param : translatableParameters)
            result = result.replace(String.format("{%s}", param), clearTranslatedString(getTranslate(param)));

        return result;
    }

    public static MutableComponent applyParametersToTemplate(MutableComponent template, Map<String, Component> parameterToComponent) {
        parameterToComponent = mergeMaps(parameterToComponent, buildTimestampParameters());

        MutableComponent result = Component.empty();

        String orPattern = parameterToComponent.keySet().stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        Pattern parameterPattern = Pattern.compile(orPattern);

        for (Component templatePart : template.toFlatList()) {
            String templateString = templatePart.getString();
            Matcher matcher = parameterPattern.matcher(templateString);

            if (!matcher.find()) {
                result.append(Component.literal(templateString).withStyle(templatePart.getStyle()));
                continue;
            }

            int currentPos = 0;

            do {
                String textBefore = templateString.substring(currentPos, matcher.start());
                result.append(Component.literal(textBefore).withStyle(templatePart.getStyle()));

                Component toReplace = parameterToComponent.get(matcher.group());
                if (toReplace != null) {
                    for (Component toReplaceInnerComp : toReplace.toFlatList()) {
                        result.append(Component.literal(toReplaceInnerComp.getString())
                                .withStyle(mergeStyles(toReplaceInnerComp.getStyle(), templatePart.getStyle()))
                        );
                    }
                }

                currentPos = matcher.end();
            } while (matcher.find());

            if (currentPos < templateString.length()) {
                String textAfter = templateString.substring(currentPos);
                result.append(Component.literal(textAfter).withStyle(templatePart.getStyle()));
            }
        }

        return result;
    }

    public static Map<String, Component> buildPositionComponentParameters(@Nullable Entity entity){
        return buildPositionParameters(entity).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Component.literal(e.getValue())));
    }

    public static Map<String, Component> buildTimestampParameters(){
        HashMap<String, Component> result = new HashMap<>();
        OffsetDateTime now = getDateTimeWithUtcOffset();
        result.put("{HH}", Component.literal(String.format("%02d", now.getHour())));
        result.put("{MM}", Component.literal(String.format("%02d", now.getMinute())));
        result.put("{SS}", Component.literal(String.format("%02d", now.getSecond())));

        return result;
    }

    public static OffsetDateTime getDateTimeWithUtcOffset(){
        Instant nowUtc = Instant.now();
        ZoneOffset offset = ZoneOffset.ofHours(ConfigProvider.getConfig().utcOffsetHours());

        return nowUtc.atOffset(offset);
    }

    public static Map<String, String> buildPositionParameters(@Nullable Entity entity){
        HashMap<String, String> result = new HashMap<>();

        if (entity == null){
            result.put(X, "");
            result.put(Y, "");
            result.put(Z, "");
            result.put(DIMENSION, "");
            return result;
        }

        BlockPos pos = entity.blockPosition();
        result.put(X, String.valueOf(pos.getX()));
        result.put(Y, String.valueOf(pos.getY()));
        result.put(Z, String.valueOf(pos.getZ()));
        result.put(DIMENSION, getDimensionName(entity.level().dimension()));

        return result;
    }

    private static String getDimensionName(ResourceKey<Level> dimension){
        if (dimension == Level.OVERWORLD)
            return "Overworld";
        if (dimension == Level.NETHER)
            return "Nether";
        if (dimension == Level.END)
            return "End";
        return prettifyDimensionPath(dimension.location().getPath());
    }

    private static String prettifyDimensionPath(String path){
        return Arrays.stream(path.split("_"))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    @SafeVarargs
    public static <T> Map<String, T> mergeMaps(Map<String, T>... parameterMaps){
        return Arrays.stream(parameterMaps)
                .flatMap(m -> m.entrySet().stream())
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private static <T> T nullSafeElse(T first, T second) {
        return first != null ? first : second;
    }

    private static Style mergeStyles(Style main, Style second){
        return Style.EMPTY.withBold(main.isBold() || second.isBold())
                .withItalic(main.isItalic() || second.isItalic())
                .withUnderlined(main.isUnderlined() || second.isUnderlined())
                .withStrikethrough(main.isStrikethrough() || second.isStrikethrough())
                .withObfuscated(main.isObfuscated() || second.isObfuscated())
                .withColor(nullSafeElse(main.getColor(), second.getColor()))
                .withClickEvent(nullSafeElse(main.getClickEvent(), second.getClickEvent()))
                .withHoverEvent(nullSafeElse(main.getHoverEvent(), second.getHoverEvent()))
                .withInsertion(nullSafeElse(main.getInsertion(), second.getInsertion()));
    }

    private static String clearTranslatedString(String text) {
        return text.replaceAll("%s|:|«|»", "").trim();
    }
}
