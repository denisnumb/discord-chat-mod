package com.denisnumb.discord_chat_mod.chat_images;

import com.denisnumb.discord_chat_mod.chat_images.model.ImageSize;
import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.chat_images.ImageStorage.MAX_HEIGHT;
import static com.denisnumb.discord_chat_mod.chat_images.ImageStorage.MAX_WIDTH;

public class ImageUtils {
    public static final String SPOILER_PREFIX = "/SPOILER_";
    public static final String LOCAL_RESOURCE_PREFIX = "localResource://";

    public static boolean isTenorGifUrl(String url) {
        return url.startsWith("https://tenor.com/");
    }

    public static boolean isImageUrl(String mimeType) {
        return mimeType.matches("(?i)^image/(png|jpeg|jpg|gif|bmp|webp)$");
    }

    public static boolean isGifUrl(String mimeType) {
        return mimeType.equalsIgnoreCase("image/gif");
    }

    public static String getMimeType(String url) {
        if (url.isEmpty())
            return "";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            return connection.getContentType();
        } catch (Exception e) {
            return "";
        }
    }

    @Nullable
    public static <K, V> V waitForLocalResource(Map<K, V> map, K key, long timeoutMillis, long pollIntervalMillis) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            synchronized (map) {
                if (map.containsKey(key)) {
                    return map.get(key);
                }
            }

            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return null;
    }


    public static boolean isLocalResourceUrl(String imageUrl){
        return imageUrl.startsWith(LOCAL_RESOURCE_PREFIX);
    }

    public static boolean isSpoilerImageUrl(String imageUrl){
        return imageUrl.contains(SPOILER_PREFIX);
    }

    public static ImageSize getImageScaledSize(int width, int height){
        if (width > MAX_WIDTH || height > MAX_HEIGHT){
            float imageScale = Math.min(MAX_WIDTH / width, MAX_HEIGHT / height);
            width = (int) (width * imageScale);
            height = (int) (height * imageScale);
        }

        return new ImageSize(width, height);
    }

    public static String getTenorGifSourceUrl(String tenorUrl) throws IOException {
        Document doc = Jsoup.connect(tenorUrl)
                .userAgent("Mozilla/5.0")
                .get();

        Element gifMeta = doc.selectFirst("meta[property=og:image]");
        if (gifMeta != null) {
            String intermediateGifUrl = gifMeta.attr("content");
            String id = extractGifId(intermediateGifUrl);
            if (id != null)
                return "https://c.tenor.com/" + id + "/tenor.gif";
        }

        throw new IOException("No tenor gif source url found");
    }

    @Nullable
    public static String extractGifId(String url) {
        int start = url.indexOf("/m/") + 3;
        int end = url.indexOf('/', start);
        if (start > 2 && end > start) {
            return url.substring(start, end);
        }

        return null;
    }

    public static String getDisposalMethod(IIOMetadata metadata) {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_gif_image_1.0");
        IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);

        return gce.getAttribute("disposalMethod");
    }

    public static Rectangle getFrameRect(IIOMetadata metadata) {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_gif_image_1.0");
        IIOMetadataNode imgDesc = (IIOMetadataNode) root.getElementsByTagName("ImageDescriptor").item(0);
        int x = Integer.parseInt(imgDesc.getAttribute("imageLeftPosition"));
        int y = Integer.parseInt(imgDesc.getAttribute("imageTopPosition"));
        int w = Integer.parseInt(imgDesc.getAttribute("imageWidth"));
        int h = Integer.parseInt(imgDesc.getAttribute("imageHeight"));

        return new Rectangle(x, y, w, h);
    }

    public static void clearBufferedImageArea(BufferedImage canvas, Rectangle rect) {
        Graphics2D g = canvas.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
        g.dispose();
    }

    public static BufferedImage bufferedImageDeepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPreMultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPreMultiplied, null);
    }

    public static int getGifFrameDuration(IIOMetadata metadata) {
        String metaFormat = metadata.getNativeMetadataFormatName();
        if (!"javax_imageio_gif_image_1.0".equals(metaFormat))
            return 100;

        Node root = metadata.getAsTree(metaFormat);
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if ("GraphicControlExtension".equals(node.getNodeName())) {
                Node delayNode = node.getAttributes().getNamedItem("delayTime");
                if (delayNode != null)
                    return Integer.parseInt(delayNode.getNodeValue()) * 10;
            }
        }

        return 100;
    }

    public static void applyPixelation(NativeImage image, int pixelSize) {
        if (pixelSize <= 1)
            return;

        int width = image.getWidth();
        int height = image.getHeight();

        NativeImage small = new NativeImage(width / pixelSize, height / pixelSize, false);

        for (int y = 0; y < small.getHeight(); y++) {
            for (int x = 0; x < small.getWidth(); x++) {
                int srcX = x * pixelSize + pixelSize / 2;
                int srcY = y * pixelSize + pixelSize / 2;

                srcX = Math.min(srcX, width - 1);
                srcY = Math.min(srcY, height - 1);

                int color = image.getPixelRGBA(srcX, srcY);
                small.setPixelRGBA(x, y, color);
            }
        }

        for (int y = 0; y < height; y++) {
            int srcY = y / pixelSize;
            for (int x = 0; x < width; x++) {
                int srcX = x / pixelSize;

                srcX = Math.min(srcX, small.getWidth() - 1);
                srcY = Math.min(srcY, small.getHeight() - 1);

                int color = small.getPixelRGBA(srcX, srcY);
                image.setPixelRGBA(x, y, color);
            }
        }

        small.close();
    }

    public static void applySpoilerOverlay(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        float scale = width >= height ? 0.65f : 0.85f;
        int boxWidth = (int)(width * scale);
        int boxHeight = (int)(height * 0.25f);

        int startX = (width - boxWidth) / 2;
        int startY = (height - boxHeight) / 2;

        int radius = Math.min(boxWidth, boxHeight) / 2;
        int overlayColor = argb(0x95, 0x00, 0x00, 0x00);

        for (int y = 0; y < boxHeight; y++) {
            for (int x = 0; x < boxWidth; x++) {
                if (!isInsideRoundedRect(x, y, boxWidth, boxHeight, radius))
                    continue;

                int px = startX + x;
                int py = startY + y;

                if (px < 0 || px >= width || py < 0 || py >= height)
                    continue;

                int baseColor = image.getPixelRGBA(px, py);
                int blended = blendARGB(baseColor, overlayColor);
                image.setPixelRGBA(px, py, blended);
            }
        }
    }

    private static boolean isInsideRoundedRect(int x, int y, int width, int height, int radius) {
        if (x >= radius && x < width - radius && y >= 0 && y < height)
            return true;
        if (y >= radius && y < height - radius && x >= 0 && x < width)
            return true;

        int dx, dy;

        // Upper left corner
        dx = radius - x;
        dy = radius - y;
        if (x < radius && y < radius && dx*dx + dy*dy <= radius*radius)
            return true;

        // Upper right corner
        dx = x - (width - radius - 1);
        dy = radius - y;
        if (x >= width - radius && y < radius && dx*dx + dy*dy <= radius*radius)
            return true;

        // Lower left corner
        dx = radius - x;
        dy = y - (height - radius - 1);
        if (x < radius && y >= height - radius && dx*dx + dy*dy <= radius*radius)
            return true;

        // Lower right corner
        dx = x - (width - radius - 1);
        dy = y - (height - radius - 1);
        return x >= width - radius && y >= height - radius && dx * dx + dy * dy <= radius * radius;
    }



    private static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                (b & 0xFF);
    }

    private static int blendARGB(int base, int overlay) {
        int baseA = (base >> 24) & 0xFF;
        int baseR = (base >> 16) & 0xFF;
        int baseG = (base >> 8)  & 0xFF;
        int baseB = base & 0xFF;

        int overlayA = (overlay >> 24) & 0xFF;
        int overlayR = (overlay >> 16) & 0xFF;
        int overlayG = (overlay >> 8)  & 0xFF;
        int overlayB = overlay & 0xFF;

        float alpha = overlayA / 255.0f;

        int outR = (int)(overlayR * alpha + baseR * (1 - alpha));
        int outG = (int)(overlayG * alpha + baseG * (1 - alpha));
        int outB = (int)(overlayB * alpha + baseB * (1 - alpha));

        return argb(baseA, outR, outG, outB);
    }
}