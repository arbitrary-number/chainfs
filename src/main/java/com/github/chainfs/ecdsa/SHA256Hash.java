/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) Arbitrary Number Project Team. All rights reserved.
 */
package com.github.chainfs.ecdsa;

import java.math.BigInteger;
import java.util.Arrays;

public class SHA256Hash {

    private static final int WORD_SIZE = 32;
    private static final BigInteger MOD = BigInteger.ONE.shiftLeft(WORD_SIZE);
    private static final BigInteger MASK = MOD.subtract(BigInteger.ONE);

    // Initial hash values h0..h7
    private static final BigInteger[] H = {
        new BigInteger("6a09e667", 16),
        new BigInteger("bb67ae85", 16),
        new BigInteger("3c6ef372", 16),
        new BigInteger("a54ff53a", 16),
        new BigInteger("510e527f", 16),
        new BigInteger("9b05688c", 16),
        new BigInteger("1f83d9ab", 16),
        new BigInteger("5be0cd19", 16)
    };

    // Constants K
    private static final BigInteger[] K = {
        new BigInteger("428a2f98",16), new BigInteger("71374491",16), new BigInteger("b5c0fbcf",16), new BigInteger("e9b5dba5",16),
        new BigInteger("3956c25b",16), new BigInteger("59f111f1",16), new BigInteger("923f82a4",16), new BigInteger("ab1c5ed5",16),
        new BigInteger("d807aa98",16), new BigInteger("12835b01",16), new BigInteger("243185be",16), new BigInteger("550c7dc3",16),
        new BigInteger("72be5d74",16), new BigInteger("80deb1fe",16), new BigInteger("9bdc06a7",16), new BigInteger("c19bf174",16),
        new BigInteger("e49b69c1",16), new BigInteger("efbe4786",16), new BigInteger("0fc19dc6",16), new BigInteger("240ca1cc",16),
        new BigInteger("2de92c6f",16), new BigInteger("4a7484aa",16), new BigInteger("5cb0a9dc",16), new BigInteger("76f988da",16),
        new BigInteger("983e5152",16), new BigInteger("a831c66d",16), new BigInteger("b00327c8",16), new BigInteger("bf597fc7",16),
        new BigInteger("c6e00bf3",16), new BigInteger("d5a79147",16), new BigInteger("06ca6351",16), new BigInteger("14292967",16),
        new BigInteger("27b70a85",16), new BigInteger("2e1b2138",16), new BigInteger("4d2c6dfc",16), new BigInteger("53380d13",16),
        new BigInteger("650a7354",16), new BigInteger("766a0abb",16), new BigInteger("81c2c92e",16), new BigInteger("92722c85",16),
        new BigInteger("a2bfe8a1",16), new BigInteger("a81a664b",16), new BigInteger("c24b8b70",16), new BigInteger("c76c51a3",16),
        new BigInteger("d192e819",16), new BigInteger("d6990624",16), new BigInteger("f40e3585",16), new BigInteger("106aa070",16),
        new BigInteger("19a4c116",16), new BigInteger("1e376c08",16), new BigInteger("2748774c",16), new BigInteger("34b0bcb5",16),
        new BigInteger("391c0cb3",16), new BigInteger("4ed8aa4a",16), new BigInteger("5b9cca4f",16), new BigInteger("682e6ff3",16),
        new BigInteger("748f82ee",16), new BigInteger("78a5636f",16), new BigInteger("84c87814",16), new BigInteger("8cc70208",16),
        new BigInteger("90befffa",16), new BigInteger("a4506ceb",16), new BigInteger("bef9a3f7",16), new BigInteger("c67178f2",16)
    };

    // Bitwise rotate right (circular right shift) for 32-bit word using BigInteger
    private static BigInteger rotr(BigInteger x, int n) {
        n = n % WORD_SIZE;
        return x.shiftRight(n).or(x.shiftLeft(WORD_SIZE - n)).and(MASK);
    }

    // Right shift
    private static BigInteger shr(BigInteger x, int n) {
        return x.shiftRight(n);
    }

    // Choice function: ch(x,y,z) = (x & y) ^ (~x & z)
    private static BigInteger ch(BigInteger x, BigInteger y, BigInteger z) {
        return x.and(y).xor(x.not().and(z)).and(MASK);
    }

    // Majority function: maj(x,y,z) = (x & y) ^ (x & z) ^ (y & z)
    private static BigInteger maj(BigInteger x, BigInteger y, BigInteger z) {
        return x.and(y).xor(x.and(z)).xor(y.and(z)).and(MASK);
    }

    // Big sigma0
    private static BigInteger bigSigma0(BigInteger x) {
        return rotr(x, 2).xor(rotr(x, 13)).xor(rotr(x, 22));
    }

    // Big sigma1
    private static BigInteger bigSigma1(BigInteger x) {
        return rotr(x, 6).xor(rotr(x, 11)).xor(rotr(x, 25));
    }

    // Small sigma0
    private static BigInteger smallSigma0(BigInteger x) {
        return rotr(x, 7).xor(rotr(x, 18)).xor(shr(x, 3));
    }

    // Small sigma1
    private static BigInteger smallSigma1(BigInteger x) {
        return rotr(x, 17).xor(rotr(x, 19)).xor(shr(x, 10));
    }

    // Add mod 2^32
    private static BigInteger addMod32(BigInteger... vals) {
        BigInteger sum = BigInteger.ZERO;
        for (BigInteger v : vals) {
            sum = sum.add(v);
        }
        return sum.and(MASK);
    }

    // Preprocessing: pad input to multiples of 512 bits (64 bytes)
    private static byte[] preprocess(byte[] input) {
        int originalLength = input.length;
        long bitLength = (long) originalLength * 8;

        // Padding: 1 bit + k zero bits + 64 bits for length
        int paddingLength = (56 - (originalLength + 1) % 64) % 64; // pad to 448 mod 512 bits
        int totalLength = originalLength + 1 + paddingLength + 8;

        byte[] padded = new byte[totalLength];
        System.arraycopy(input, 0, padded, 0, originalLength);

        padded[originalLength] = (byte) 0x80; // append '1' bit

        // Append length in bits as 64-bit big-endian integer
        for (int i = 0; i < 8; i++) {
            padded[totalLength - 1 - i] = (byte) ((bitLength >>> (8 * i)) & 0xff);
        }

        return padded;
    }

    // Convert 4 bytes to BigInteger word (unsigned 32-bit)
    private static BigInteger toWord(byte[] data, int offset) {
        int val = ((data[offset] & 0xff) << 24) |
                  ((data[offset + 1] & 0xff) << 16) |
                  ((data[offset + 2] & 0xff) << 8) |
                  ((data[offset + 3] & 0xff));
        return new BigInteger(Integer.toUnsignedString(val));
    }

    // Main hash function
    public static byte[] sha256(byte[] message) {
        byte[] padded = preprocess(message);

        BigInteger[] h = Arrays.copyOf(H, H.length);

        int numBlocks = padded.length / 64;

        for (int i = 0; i < numBlocks; i++) {
            BigInteger[] w = new BigInteger[64];

            // Prepare message schedule
            for (int t = 0; t < 16; t++) {
                w[t] = toWord(padded, i * 64 + t * 4);
            }
            for (int t = 16; t < 64; t++) {
                BigInteger s0 = smallSigma0(w[t - 15]);
                BigInteger s1 = smallSigma1(w[t - 2]);
                w[t] = addMod32(w[t - 16], s0, w[t - 7], s1);
            }

            // Initialize working vars
            BigInteger a = h[0];
            BigInteger b = h[1];
            BigInteger c = h[2];
            BigInteger d = h[3];
            BigInteger e = h[4];
            BigInteger f = h[5];
            BigInteger g = h[6];
            BigInteger hh = h[7];

            // Main compression loop
            for (int t = 0; t < 64; t++) {
                BigInteger S1 = bigSigma1(e);
                BigInteger ch = ch(e, f, g);
                BigInteger temp1 = addMod32(hh, S1, ch, K[t], w[t]);
                BigInteger S0 = bigSigma0(a);
                BigInteger maj = maj(a, b, c);
                BigInteger temp2 = addMod32(S0, maj);

                hh = g;
                g = f;
                f = e;
                e = addMod32(d, temp1);
                d = c;
                c = b;
                b = a;
                a = addMod32(temp1, temp2);
            }

            // Add chunk to hash values
            h[0] = addMod32(h[0], a);
            h[1] = addMod32(h[1], b);
            h[2] = addMod32(h[2], c);
            h[3] = addMod32(h[3], d);
            h[4] = addMod32(h[4], e);
            h[5] = addMod32(h[5], f);
            h[6] = addMod32(h[6], g);
            h[7] = addMod32(h[7], hh);
        }

        // Produce final hash bytes
        byte[] hash = new byte[32];
        for (int i = 0; i < 8; i++) {
            byte[] part = h[i].toByteArray();

            // h[i] might be shorter than 4 bytes if leading zeros, so pad:
            byte[] paddedPart = new byte[4];
            int copyStart = Math.max(0, part.length - 4);
            int copyLen = Math.min(part.length, 4);
            System.arraycopy(part, copyStart, paddedPart, 4 - copyLen, copyLen);

            // Copy to output
            System.arraycopy(paddedPart, 0, hash, i * 4, 4);
        }

        return hash;
    }

    // Helper to convert bytes to hex string
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String input = "hello";
        byte[] hash = sha256(input.getBytes());
        System.out.println("SHA-256 hash: " + toHex(hash));
    }
}
