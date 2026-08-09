package com.denisnumb.discord_chat_mod.chat_images.clipboard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MacOSClipboardImageReader {
    public byte[] read() throws IllegalStateException {
        try {
            Process process = new ProcessBuilder(
                    "osascript", "-e",
                    "set theData to the clipboard as «class PNGf»\n" +
                            "return theData"
            ).start();

            byte[] result = readStream(process.getInputStream());
            int exitCode = process.waitFor();

            if (exitCode != 0 || result.length == 0) {
                return readAsTiff();
            }

            return parseOsascriptHex(result);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] readAsTiff() throws Exception {
        Process process = new ProcessBuilder(
                "osascript", "-e",
                "set theData to the clipboard as «class TIFF»\n" +
                        "return theData"
        ).start();

        byte[] result = readStream(process.getInputStream());
        int exitCode = process.waitFor();

        if (exitCode != 0 || result.length == 0)
            return new byte[0];

        byte[] tiffBytes = parseOsascriptHex(result);
        return tiffToPng(tiffBytes);
    }

    private byte[] parseOsascriptHex(byte[] raw) {
        String text = new String(raw).trim();

        int dataStart;
        if (text.contains("TIFF")) {
            dataStart = text.indexOf("TIFF") + 4;
        } else if (text.contains("PNGf")) {
            dataStart = text.indexOf("PNGf") + 4;
        } else {
            return new byte[0];
        }

        String hex = text.substring(dataStart).replace("»", "").trim();

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private byte[] tiffToPng(byte[] tiffBytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(tiffBytes));
        if (image == null)
            return new byte[0];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }
}