package com.example.task2;

import org.json.JSONObject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class TLVEncoder {

    // Function to encode JSON to TLV format
    public static byte[] encodeToTLV(byte tag, JSONObject jsonObject) {
        // Convert JSON object to byte array
        byte[] valueBytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        // Determine the length of the value
        int length = valueBytes.length;

        // Convert length to byte array (assuming length fits in one byte)
        byte[] lengthBytes = ByteBuffer.allocate(4).putInt(length).array(); // 4-byte length

        // Combine Tag + Length + Value
        byte[] tlvBytes = new byte[1 + lengthBytes.length + valueBytes.length];
        tlvBytes[0] = tag;  // Set the tag
        System.arraycopy(lengthBytes, 0, tlvBytes, 1, lengthBytes.length); // Copy length bytes
        System.arraycopy(valueBytes, 0, tlvBytes, 1 + lengthBytes.length, valueBytes.length); // Copy value

        return tlvBytes;
    }
}
