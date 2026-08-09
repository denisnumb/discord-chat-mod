package com.denisnumb.discord_chat_mod.chat_images.clipboard;

import com.sun.jna.*;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WindowsClipboardImageReader {
    private static final int CF_DIB = 8;

    interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class);

        boolean OpenClipboard(HWND hWndNewOwner);

        void CloseClipboard();

        HANDLE GetClipboardData(int uFormat);

        boolean IsClipboardFormatAvailable(int format);
    }

    interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        Pointer GlobalLock(HANDLE hMem);

        void GlobalUnlock(HANDLE hMem);

        BaseTSD.SIZE_T GlobalSize(HANDLE hMem);
    }

    public byte[] read() throws IllegalStateException {
        // if buffer has no image
        if (!User32.INSTANCE.IsClipboardFormatAvailable(CF_DIB)) {
            return new byte[0];
        }

        // if can't open clipboard
        if (!User32.INSTANCE.OpenClipboard(null)) {
            return new byte[0];
        }

        try {
            HANDLE hDib = User32.INSTANCE.GetClipboardData(CF_DIB);
            if (hDib == null)
                return new byte[0];

            Pointer ptr = Kernel32.INSTANCE.GlobalLock(hDib);
            if (ptr == null)
                return new byte[0];

            try {
                BaseTSD.SIZE_T size = Kernel32.INSTANCE.GlobalSize(hDib);
                byte[] data = ptr.getByteArray(0, size.intValue());
                return dibToPng(data);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                Kernel32.INSTANCE.GlobalUnlock(hDib);
            }
        } finally {
            User32.INSTANCE.CloseClipboard();
        }
    }

    private byte[] dibToPng(byte[] dib) throws Exception {
        ByteBuffer buf = ByteBuffer.wrap(dib).order(ByteOrder.LITTLE_ENDIAN);

        int headerSize = buf.getInt();   // biSize (40)
        int width = buf.getInt();        // biWidth
        int height = buf.getInt();       // biHeight (negative = top-down)
        buf.getShort();                  // biPlanes
        short bitCount = buf.getShort(); // biBitCount (24 or 32)
        int compression = buf.getInt();  // biCompression (0 = BI_RGB)

        // skip other fields
        buf.position(headerSize);

        boolean topDown = height < 0;
        int absHeight = Math.abs(height);

        BufferedImage image = new BufferedImage(width, absHeight, BufferedImage.TYPE_INT_ARGB);

        int bytesPerPixel = bitCount / 8;
        // Rows in DIB are 4 byte aligned
        int rowSize = ((width * bytesPerPixel + 3) / 4) * 4;

        for (int y = 0; y < absHeight; y++) {
            // bottom-up: lines from bottom to top
            int srcRow = topDown ? y : (absHeight - 1 - y);
            buf.position(headerSize + srcRow * rowSize);

            for (int x = 0; x < width; x++) {
                int b = buf.get() & 0xFF;
                int g = buf.get() & 0xFF;
                int r = buf.get() & 0xFF;
                int a = (bytesPerPixel == 4) ? (buf.get() & 0xFF) : 0xFF;

                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}