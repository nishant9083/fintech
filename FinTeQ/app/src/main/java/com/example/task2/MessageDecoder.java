package com.example.task2;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import kotlin.UByte;

public class MessageDecoder {

        public static byte[] convertStringToByteArray(String input) {
            // Remove brackets and spaces, then split by commas
            String[] parts = input.replace("[", "").replace("]", "").trim().split(",");

            // Convert each string number to a byte
            byte[] byteArray = new byte[parts.length];
            for (int i = 0; i < parts.length; i++) {
                byteArray[i] = (byte) Integer.parseInt(parts[i].trim());
            }

            return byteArray;
        }

    public static JSONObject decodeMessage(String msg) throws JSONException {
            byte[] message = convertStringToByteArray(msg);
        if (message == null || message.length < 2) {
            throw new IllegalArgumentException("Invalid message format");
        }

        // Extract the Tag (1 byte)
        byte tag = message[0];
        int i2 = tag & UByte.MAX_VALUE;
        String tg = Integer.toHexString(i2);

        int intValue = (int) Long.parseLong(tg, 16);
        byte byteValue = (byte) intValue;
        String parsedHex = String.format("%02x", byteValue & 0xFF);

        // Extract Length (assuming next bytes represent length, variable-sized)
        int lengthIndex = 1;
        int length = 0;

        // Decode length assuming it uses multiple bytes if needed
        while (message[lengthIndex] == 0) {
            lengthIndex++;
        }
        length = message[lengthIndex];

        // Extract value
        int valueStartIndex = lengthIndex + 1;
        byte[] valueBytes = Arrays.copyOfRange(message, valueStartIndex, message.length);

        // Convert value bytes to a string
        String jsonString = new String(valueBytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(jsonString);
        jsonObject.put("tag", parsedHex);
        // Parse JSON
        return jsonObject;
    }
}

