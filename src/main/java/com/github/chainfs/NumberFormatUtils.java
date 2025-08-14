package com.github.chainfs;
import java.math.BigInteger;
import org.bouncycastle.util.encoders.Hex;

public class NumberFormatUtils {

    public static String concatXY(BigInteger x, BigInteger y, int coordinateLengthBytes) {
        byte[] xBytes = toFixedLengthBytes(x, coordinateLengthBytes);
        byte[] yBytes = toFixedLengthBytes(y, coordinateLengthBytes);

        byte[] concatenated = new byte[xBytes.length + yBytes.length];
        System.arraycopy(xBytes, 0, concatenated, 0, xBytes.length);
        System.arraycopy(yBytes, 0, concatenated, xBytes.length, yBytes.length);

        return Hex.toHexString(concatenated);
    }

    private static byte[] toFixedLengthBytes(BigInteger value, int length) {
        byte[] full = value.toByteArray();
        if (full.length == length) return full;

        byte[] result = new byte[length];
        if (full.length > length) {
            // value is longer than expected, trim leading byte (could be sign byte)
            System.arraycopy(full, full.length - length, result, 0, length);
        } else {
            // pad with leading zeros
            System.arraycopy(full, 0, result, length - full.length, full.length);
        }
        return result;
    }
}
