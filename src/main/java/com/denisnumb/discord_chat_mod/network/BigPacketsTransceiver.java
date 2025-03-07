package com.denisnumb.discord_chat_mod.network;

import java.util.*;

public class BigPacketsTransceiver {
    @FunctionalInterface
    public interface SendFunction {
        void send(long sendTime, int partIndex, int totalParts, byte[] part);
    }

    private static final int MAX_PART_SIZE = 32000;

    public static void send(byte[] data, long sendTime, SendFunction sendFunction) {
        int totalParts = (int) Math.ceil(data.length / (double) MAX_PART_SIZE);

        for (int i = 0; i < totalParts; i++) {
            int start = i * MAX_PART_SIZE;
            int end = Math.min(start + MAX_PART_SIZE, data.length);
            byte[] part = Arrays.copyOfRange(data, start, end);
            sendFunction.send(sendTime, i, totalParts, part);
        }
    }

    public static Optional<byte[]> receivePart(
            Map<Long, ArrayList<byte[]>> receivedParts,
            long sendTime,
            int partIndex,
            int totalParts,
            byte[] partData
    ) {
        receivedParts.putIfAbsent(sendTime, new ArrayList<>());
        receivedParts.get(sendTime).add(partIndex, partData);

        if (receivedParts.get(sendTime).size() == totalParts) {
            byte[] data = mergeParts(receivedParts.get(sendTime));
            receivedParts.remove(sendTime);
            return Optional.of(data);
        }
        return Optional.empty();
    }

    private static byte[] mergeParts(ArrayList<byte[]> parts) {
        int totalSize = parts.stream().mapToInt(arr -> arr.length).sum();
        byte[] result = new byte[totalSize];

        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, offset, part.length);
            offset += part.length;
        }

        return result;
    }
}
