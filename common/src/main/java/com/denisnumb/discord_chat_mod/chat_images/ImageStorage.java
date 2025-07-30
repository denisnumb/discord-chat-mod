package com.denisnumb.discord_chat_mod.chat_images;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.chat_images.model.*;
import com.denisnumb.discord_chat_mod.chat_images.model.Image;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.LOGGER;
import static com.denisnumb.discord_chat_mod.chat_images.ImageUtils.*;
import static com.denisnumb.discord_chat_mod.chat_images.ImageUtils.getGifFrameDuration;

public class ImageStorage {
    public static final float MAX_WIDTH = 128.0f;
    public static final float MAX_HEIGHT = 72.0f;

    public static final Map<String, AbstractImage> IMAGE_CACHE = new HashMap<>();
    public static final Set<String> HANDLED_URLS = new HashSet<>();
    private static CompletableFuture<LoadResult> lastTask = CompletableFuture.completedFuture(null);

    public static CompletableFuture<LoadResult> loadImagesParallel(
            List<String> urls,
            Supplier<Integer> trimmedSize,
            Supplier<Integer> allSize
    ) {
        synchronized (ImageStorage.class) {
            lastTask = lastTask.thenComposeAsync(ignored -> runLoadImages(urls, trimmedSize.get(), allSize.get()));
            return lastTask;
        }
    }

    private static CompletableFuture<LoadResult> runLoadImages(List<String> urls, int trimmedSize, int allSize) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<CompletableFuture<AbstractImage>> futures = urls.stream().distinct()
                .map(url -> CompletableFuture.supplyAsync(() -> parseImage(url), executor))
                .toList();

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> new LoadResult(
                        trimmedSize,
                        allSize,
                        urls.stream().map(ImageStorage::parseImage).toList()
                ))
                .whenComplete((res, ex) -> executor.shutdown());
    }

    @Nullable
    public static AbstractImage parseEmoji(String url) {
        return parseImageInternal(url, false);
    }

    @Nullable
    private static AbstractImage parseImage(String url) {
        return parseImageInternal(url, true);
    }

    @Nullable
    private static AbstractImage parseImageInternal(String url, boolean skipHandledUrls) {
        if (IMAGE_CACHE.containsKey(url))
            return IMAGE_CACHE.get(url);
        if (skipHandledUrls && HANDLED_URLS.contains(url))
            return null;
        if (skipHandledUrls)
            HANDLED_URLS.add(url);

        String mimeType = getMimeType(url);

        if (isImageUrl(mimeType) || isTenorGifUrl(url)) {
            try {
                if (isTenorGifUrl(url) || isGifUrl(mimeType))
                    loadGifFromUrl(url);
                else
                    loadImageFromUrl(url);
                return parseImageInternal(url, skipHandledUrls);
            } catch (Exception e) {
                LOGGER.error("ImageLoadError: {}", e.getMessage());
            }
        } else if (isLocalResourceUrl(url)) {
            return waitForLocalResource(IMAGE_CACHE, url, 15000, 50);
        }

        return null;
    }

    public static void registerImage(String imageUrl, byte[] imageBytes) throws IOException, InterruptedException {
        NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(imageBytes));

        boolean isSpoiler = ImageUtils.isSpoilerImageUrl(imageUrl);
        ResourceLocation spoilerTextureLocation = isSpoiler
                ? registerSpoilerImage(imageUrl, NativeImage.read(new ByteArrayInputStream(imageBytes)))
                : null;
        ResourceLocation textureLocation = new ResourceLocation(
                DiscordChatMod.MOD_ID + "/chat_image/" + imageUrl.hashCode()
        );

        CountDownLatch latch = new CountDownLatch(1);
        RenderSystem.recordRenderCall(() -> {
            try {
                Minecraft.getInstance().getTextureManager().register(textureLocation, new DynamicTexture(nativeImage));
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        IMAGE_CACHE.put(imageUrl, new Image(
                imageUrl,
                getImageScaledSize(nativeImage.getWidth(), nativeImage.getHeight()),
                new ImageSize(nativeImage.getWidth(), nativeImage.getHeight()),
                textureLocation,
                isSpoiler,
                spoilerTextureLocation
        ));
    }

    private static void loadImageFromUrl(String imageUrl) throws IOException, InterruptedException, URISyntaxException {
        try (InputStream inputStream = new URI(imageUrl).toURL().openStream()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(ImageIO.read(inputStream), "png", outputStream);
            registerImage(imageUrl, outputStream.toByteArray());
        }
    }

    public static void loadGifFromUrl(String gifUrl) throws Exception {
        String cacheKey = gifUrl;
        if (isTenorGifUrl(gifUrl))
            gifUrl = getTenorGifSourceUrl(gifUrl);

        List<ResourceLocation> frames = new ArrayList<>();
        ImageSize frameSize = null;
        int frameDuration = -1;

        boolean isSpoiler = ImageUtils.isSpoilerImageUrl(gifUrl);
        ResourceLocation spoilerTextureLocation = null;

        try (ImageInputStream input = ImageIO.createImageInputStream(new URI(gifUrl).toURL().openStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) throw new IOException("No GIF reader found");

            ImageReader reader = readers.next();
            reader.setInput(input);
            int numFrames = reader.getNumImages(true);
            CountDownLatch latch = new CountDownLatch(numFrames);

            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = canvas.createGraphics();
            g2d.setBackground(new Color(0, 0, 0, 0));
            g2d.clearRect(0, 0, width, height);

            BufferedImage savedCanvas = null;

            for (int i = 0; i < numFrames; i++) {
                BufferedImage frame = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);
                Rectangle frameRect = getFrameRect(metadata);
                String disposal = getDisposalMethod(metadata);

                switch (disposal) {
                    case "restoreToPrevious":
                        savedCanvas = bufferedImageDeepCopy(canvas);
                        break;
                    case "restoreToBackgroundColor":
                        clearBufferedImageArea(canvas, frameRect);
                        break;
                }

                Graphics2D g = canvas.createGraphics();
                g.drawImage(frame, frameRect.x, frameRect.y, null);
                g.dispose();

                if ("restoreToPrevious".equals(disposal) && savedCanvas != null) {
                    Graphics2D gRestore = canvas.createGraphics();
                    gRestore.drawImage(savedCanvas, 0, 0, null);
                    gRestore.dispose();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(canvas, "png", outputStream);
                byte[] imageBytes = outputStream.toByteArray();

                if (i == 0) {
                    frameSize = new ImageSize(frame.getWidth(), frame.getHeight());
                    frameDuration = getGifFrameDuration(metadata);

                    if (isSpoiler)
                        spoilerTextureLocation = registerSpoilerImage(gifUrl, NativeImage.read(new ByteArrayInputStream(imageBytes)));
                }

                ResourceLocation frameLocation = new ResourceLocation(
                        DiscordChatMod.MOD_ID + "/chat_gif/" + gifUrl.hashCode() + "_" + i
                );

                NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(imageBytes));
                RenderSystem.recordRenderCall(() -> {
                    try {
                        Minecraft.getInstance().getTextureManager().register(frameLocation, new DynamicTexture(nativeImage));
                    } finally {
                        latch.countDown();
                    }
                });

                frames.add(frameLocation);
            }

            g2d.dispose();
            reader.dispose();
            latch.await();

            if (!frames.isEmpty()) {
                IMAGE_CACHE.put(cacheKey, new AnimatedImage(
                        cacheKey,
                        frames,
                        getImageScaledSize(frameSize.width(), frameSize.height()),
                        frameSize,
                        Math.max(frameDuration, 100),
                        isSpoiler,
                        spoilerTextureLocation
                ));
            }
        }
    }

    private static ResourceLocation registerSpoilerImage(String imageUrl, NativeImage image) throws InterruptedException {
        ImageUtils.applyPixelation(image, image.getHeight() / 6);
        ImageUtils.applySpoilerOverlay(image);
        ResourceLocation spoilerTextureLocation = new ResourceLocation(
                DiscordChatMod.MOD_ID + "/chat_image_spoiler/" + imageUrl.hashCode()
        );

        CountDownLatch latch = new CountDownLatch(1);
        RenderSystem.recordRenderCall(() -> {
            try {
                Minecraft.getInstance().getTextureManager().register(spoilerTextureLocation, new DynamicTexture(image));
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        return spoilerTextureLocation;
    }
}