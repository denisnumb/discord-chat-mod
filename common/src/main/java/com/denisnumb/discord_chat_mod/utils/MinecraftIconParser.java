package com.denisnumb.discord_chat_mod.utils;

import com.denisnumb.discord_chat_mod.BuildConstants;
import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MinecraftIconParser {
    private MinecraftIconParser() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private static final String VANILLA_ICONS_URL = "https://raw.githubusercontent.com/denisnumb/discord-chat-mod/"
            + BuildConstants.MINECRAFT_VERSION + "/data/minecraft_item_icons/";
    public static final String DEFAULT_ATTACHMENT_NAME = "icon.png";
    public static final String DEFAULT_ATTACHMENT_URL = getAttachmentUrl(DEFAULT_ATTACHMENT_NAME);

    private static final int ICON_SIZE = 64;

    private static final Map<String, String[]> CUBE_TEMPLATES = Map.of(
            "block/cube", new String[]{"up", "east", "north"},
            "block/cube_all", new String[]{"all", "all", "all"},
            "block/cube_all_inner_faces", new String[]{"all", "all", "all"},
            "block/cube_mirrored_all", new String[]{"all", "all", "all"},
            "block/leaves", new String[]{"all", "all", "all"},
            "block/cube_column", new String[]{"end", "side", "side"},
            "block/cube_bottom_top", new String[]{"top", "side", "side"},
            "block/cube_top", new String[]{"top", "side", "side"},
            "block/orientable", new String[]{"top", "side", "front"},
            "block/orientable_with_bottom", new String[]{"top", "side", "front"}
    );

    private static final double[][] CUBE_FACE_TRANSFORMS = {
            {-56.569, 28.284, -56.569, -28.284, 120.569, 29.359},
            {56.569, 28.284, 0, 69.282, 7.431, 29.359},
            {56.569, -28.284, 0, 69.282, 64, 57.643}
    };

    private static final float[] CUBE_FACE_LIGHT = {1.0f, 0.6505f, 0.4f};

    public record MinecraftIcon(String url, @Nullable FileUpload attachment) { }

    private record ResolvedModel(Map<String, JsonElement> textures, @Nullable String template, boolean hasElements) { }

    public static Optional<MinecraftIcon> parseAdvancementIcon(DisplayInfo displayInfo) {
        return parseItemIcon(displayInfo.getIcon());
    }

    public static Optional<MinecraftIcon> parseItemIcon(ItemStack stack) {
        return parseItemIcon(stack, DEFAULT_ATTACHMENT_NAME);
    }

    public static Optional<MinecraftIcon> parseItemIcon(ItemStack stack, String attachmentName) {
        if (stack.isEmpty())
            return Optional.empty();

        Identifier model = getItemModel(stack);

        if (model.getNamespace().equals("minecraft"))
            return Optional.of(new MinecraftIcon(VANILLA_ICONS_URL + model.getPath() + ".png", null));

        try {
            BufferedImage icon = renderModdedIcon(model.getNamespace(), model.getPath());
            if (icon != null)
                return Optional.of(new MinecraftIcon(getAttachmentUrl(attachmentName), FileUpload.fromData(toPng(icon), attachmentName)));
            LOGGER.debug("No icon for item model [{}]", model);
        } catch (Exception e) {
            LOGGER.error("MinecraftIconParseError [{}]: {}", model, e.getMessage());
        }

        return Optional.empty();
    }

    public static String getAttachmentUrl(String attachmentName) {
        return "attachment://" + attachmentName;
    }

    private static Identifier getItemModel(ItemStack stack) {
        Identifier model = stack.get(DataComponents.ITEM_MODEL);
        return model != null ? model : BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    @Nullable
    private static BufferedImage renderModdedIcon(String namespace, String path) throws IOException {
        JsonObject definition = readJson(String.format("assets/%s/items/%s.json", namespace, path));
        String modelId = definition != null && definition.has("model")
                ? selectModel(definition.getAsJsonObject("model"))
                : namespace + ":item/" + path;
        if (modelId == null)
            return null;

        ResolvedModel model = resolveModel(modelId);
        Map<String, JsonElement> textures = model.textures();

        if (textures.containsKey("layer0"))
            return drawFlatLayers(textures);

        String[] cubeFaces = model.hasElements() || model.template() == null ? null : CUBE_TEMPLATES.get(model.template());
        if (cubeFaces != null)
            return drawCube(textures, cubeFaces);

        BufferedImage itemTexture = loadSprite(namespace + ":item/" + path);
        if (itemTexture != null)
            return drawFlat(itemTexture);

        List<String> variables = new ArrayList<>(List.of("particle", "cross"));
        variables.addAll(textures.keySet());
        for (String variable : variables) {
            BufferedImage sprite = loadSprite(resolveTexture(textures, variable));
            if (sprite != null)
                return drawFlat(sprite);
        }
        return null;
    }

    @Nullable
    private static String selectModel(JsonObject node) {
        String type = getVanillaKey(node, "type");

        switch (type) {
            case "model", "special" -> {
                String key = type.equals("model") ? "model" : "base";
                return node.has(key) ? node.get(key).getAsString() : null;
            }
            case "condition" -> {
                return selectModel(node.getAsJsonObject("on_false"));
            }
            case "composite" -> {
                for (JsonElement child : node.getAsJsonArray("models")) {
                    String model = selectModel(child.getAsJsonObject());
                    if (model != null)
                        return model;
                }
                return null;
            }
            case "select", "range_dispatch" -> {
                JsonArray options = node.has("cases") ? node.getAsJsonArray("cases") : node.getAsJsonArray("entries");
                if (options != null && getVanillaKey(node, "property").equals("display_context"))
                    for (JsonElement option : options)
                        if (option.getAsJsonObject().get("when").toString().contains("\"gui\""))
                            return selectModel(option.getAsJsonObject().getAsJsonObject("model"));

                if (node.has("fallback"))
                    return selectModel(node.getAsJsonObject("fallback"));
                return options == null || options.isEmpty() ? null : selectModel(options.get(0).getAsJsonObject().getAsJsonObject("model"));
            }
            default -> {
                return null;
            }
        }
    }

    private static String getVanillaKey(JsonObject node, String key) {
        return node.has(key) ? node.get(key).getAsString().replace("minecraft:", "") : "";
    }

    private static ResolvedModel resolveModel(String modelId) throws IOException {
        Map<String, JsonElement> textures = new HashMap<>();
        boolean hasElements = false;
        String current = modelId;

        for (int depth = 0; current != null && depth < 32; depth++) {
            String namespace = current.contains(":") ? current.substring(0, current.indexOf(':')) : "minecraft";
            String path = current.substring(current.indexOf(':') + 1);
            if (namespace.equals("minecraft"))
                return new ResolvedModel(textures, path, hasElements);

            JsonObject model = readJson(String.format("assets/%s/models/%s.json", namespace, path));
            if (model == null)
                break;

            if (model.has("textures"))
                model.getAsJsonObject("textures").entrySet().forEach(e -> textures.putIfAbsent(e.getKey(), e.getValue()));
            hasElements |= model.has("elements");
            current = model.has("parent") ? model.get("parent").getAsString() : null;
        }

        return new ResolvedModel(textures, null, hasElements);
    }

    @Nullable
    private static String resolveTexture(Map<String, JsonElement> textures, String variable) {
        JsonElement value = textures.get(variable);
        for (int depth = 0; value != null && depth < 16; depth++) {
            if (value.isJsonObject())
                value = value.getAsJsonObject().get("sprite");
            if (value == null || !value.isJsonPrimitive())
                return null;

            String texture = value.getAsString();
            if (!texture.startsWith("#"))
                return texture;
            value = textures.get(texture.substring(1));
        }
        return null;
    }

    @Nullable
    private static BufferedImage drawFlatLayers(Map<String, JsonElement> textures) throws IOException {
        List<BufferedImage> layers = new ArrayList<>();
        for (int layer = 0; textures.containsKey("layer" + layer); layer++) {
            BufferedImage sprite = loadSprite(resolveTexture(textures, "layer" + layer));
            if (sprite != null)
                layers.add(sprite);
        }
        return layers.isEmpty() ? null : drawFlat(layers.toArray(BufferedImage[]::new));
    }

    private static BufferedImage drawFlat(BufferedImage... layers) {
        BufferedImage icon = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = createGraphics(icon);
        for (BufferedImage layer : layers)
            graphics.drawImage(layer, 0, 0, ICON_SIZE, ICON_SIZE, null);
        graphics.dispose();
        return icon;
    }

    @Nullable
    private static BufferedImage drawCube(Map<String, JsonElement> textures, String[] faceVariables) throws IOException {
        BufferedImage icon = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = createGraphics(icon);
        boolean drawn = false;

        for (int face = 0; face < 3; face++) {
            BufferedImage sprite = loadSprite(resolveTexture(textures, faceVariables[face]));
            if (sprite == null)
                continue;
            AffineTransform transform = AffineTransform.getScaleInstance(ICON_SIZE / 128.0, ICON_SIZE / 128.0);
            transform.concatenate(new AffineTransform(CUBE_FACE_TRANSFORMS[face]));
            transform.scale(1.0 / sprite.getWidth(), 1.0 / sprite.getHeight());
            graphics.drawImage(shade(sprite, CUBE_FACE_LIGHT[face]), transform, null);
            drawn = true;
        }

        graphics.dispose();
        return drawn ? icon : null;
    }

    private static Graphics2D createGraphics(BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        return graphics;
    }

    private static BufferedImage shade(BufferedImage sprite, float light) {
        BufferedImage shaded = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < sprite.getHeight(); y++) {
            for (int x = 0; x < sprite.getWidth(); x++) {
                int argb = sprite.getRGB(x, y);
                int r = Math.round((argb >> 16 & 0xFF) * light);
                int g = Math.round((argb >> 8 & 0xFF) * light);
                int b = Math.round((argb & 0xFF) * light);
                shaded.setRGB(x, y, argb & 0xFF000000 | r << 16 | g << 8 | b);
            }
        }
        return shaded;
    }

    @Nullable
    private static BufferedImage loadSprite(@Nullable String texture) throws IOException {
        if (texture == null)
            return null;

        String namespace = texture.contains(":") ? texture.substring(0, texture.indexOf(':')) : "minecraft";
        String path = texture.substring(texture.indexOf(':') + 1);

        BufferedImage image;
        try (InputStream input = getResourceAsStream(String.format("assets/%s/textures/%s.png", namespace, path))) {
            if (input == null || (image = ImageIO.read(input)) == null)
                return null;
        }

        int width = image.getWidth();
        int height = Math.min(image.getHeight(), width);
        BufferedImage sprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        boolean grayscale = !(image.getColorModel() instanceof IndexColorModel)
                && image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY;
        Raster raster = image.getRaster();
        int maxSample = (1 << image.getColorModel().getComponentSize(0)) - 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!grayscale) {
                    sprite.setRGB(x, y, image.getRGB(x, y));
                    continue;
                }
                int value = raster.getSample(x, y, 0) * 255 / maxSample;
                int alpha = raster.getNumBands() > 1 ? raster.getSample(x, y, 1) * 255 / maxSample : 255;
                sprite.setRGB(x, y, alpha << 24 | value << 16 | value << 8 | value);
            }
        }
        return sprite;
    }

    @Nullable
    private static JsonObject readJson(String path) throws IOException {
        try (InputStream input = getResourceAsStream(path)) {
            return input == null ? null : GSON.fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), JsonObject.class);
        }
    }

    private static byte[] toPng(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        }
    }

    @Nullable
    private static InputStream getResourceAsStream(String path) {
        InputStream input = DiscordChatMod.class.getResourceAsStream("/" + path);
        if (input != null)
            return input;

        return DiscordChatMod.class.getClassLoader().getResourceAsStream(path);
    }
}
