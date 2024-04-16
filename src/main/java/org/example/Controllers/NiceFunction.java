package org.example.Controllers;


public class NiceFunction {
    private static final byte sizeByte = 8;
    public static String[] getColumnsName(int columns) {
        String[] columnsName = new String[columns];
        for (int i = 0; i < columnsName.length; i++) {
            columnsName[i] = String.format("%02X", i);
        }

        return columnsName;
    }

    public static long parseUnsignedHex(String text, int maxHexSize) {
        if (text.length() == maxHexSize) {
            return (parseUnsignedHex(text.substring(0, 1), maxHexSize) << (maxHexSize * 4 - (sizeByte / 2)))
                    | parseUnsignedHex(text.substring(1), maxHexSize);
        }
        return Long.parseLong(text, 16);
    }

    public static byte[] convertHexToBytes(String hex) {
        if (hex.length() % 2 == 1) {
            hex = "0" + hex;
        }
        return javax.xml.bind.DatatypeConverter.parseHexBinary(hex);
    }
}
