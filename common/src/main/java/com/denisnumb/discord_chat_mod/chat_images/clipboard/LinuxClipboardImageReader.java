package com.denisnumb.discord_chat_mod.chat_images.clipboard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LinuxClipboardImageReader {
    private static Boolean xclipAvailable = null;
    private static Boolean wlPasteAvailable = null;

    public byte[] read() throws IllegalStateException {
        String wayland = System.getenv("WAYLAND_DISPLAY");
        return wayland != null ? readWayland() : readX11();
    }

    private byte[] readX11() throws IllegalStateException {
        if (!isXclipAvailable()) {
            throw new IllegalStateException("xclip is not available");
        }

        try {
            Process process = new ProcessBuilder(
                    "xclip", "-selection", "clipboard", "-t", "image/png", "-o"
            ).start();

            byte[] result = readStream(process.getInputStream());
            int exitCode = process.waitFor();

            if (exitCode != 0 || result.length == 0)
                return new byte[0];
            return result;
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] readWayland() throws IllegalStateException {
        if (!isWlPasteAvailable()) {
            throw new IllegalStateException("wl-paste is not available");
        }

        try {
            Process process = new ProcessBuilder(
                    "wl-paste", "--type", "image/png"
            ).start();

            byte[] result = readStream(process.getInputStream());
            int exitCode = process.waitFor();

            if (exitCode != 0 || result.length == 0)
                return new byte[0];
            return result;
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isXclipAvailable() {
        if (xclipAvailable == null) {
            xclipAvailable = checkToolAvailable("xclip");
        }
        return xclipAvailable;
    }

    private boolean isWlPasteAvailable() {
        if (wlPasteAvailable == null) {
            wlPasteAvailable = checkToolAvailable("wl-paste");
        }
        return wlPasteAvailable;
    }

    private boolean checkToolAvailable(String tool) {
        try {
            Process p = new ProcessBuilder("which", tool).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
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