package com.gag4.g4wallet.emv;

import java.util.HashMap;
import java.util.Map;

public class TLVParser {
    public static Map<Integer, byte[]> parse(byte[] data) {
        Map<Integer, byte[]> result = new HashMap<>();
        int i = 0;
        while (i < data.length) {
            int tag = data[i] & 0xFF;
            i++;
            if ((tag & 0x1F) == 0x1F) {
                tag = (tag << 8) | (data[i] & 0xFF);
                i++;
            }
            int length = data[i] & 0xFF;
            i++;
            if (length == 0x81) {
                length = data[i] & 0xFF;
                i++;
            } else if (length == 0x82) {
                length = ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
                i += 2;
            }
            if (i + length > data.length) break;
            byte[] value = new byte[length];
            System.arraycopy(data, i, value, 0, length);
            i += length;
            result.put(tag, value);
        }
        return result;
    }
}
