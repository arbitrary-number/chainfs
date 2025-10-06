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

public final class RFC6979Nonce {

    private static final int HASH_SIZE = 32; // SHA-256 output length bytes

    /**
     * Generate deterministic nonce k per RFC6979 using pure Java HMAC-SHA256.
     * @param hash message hash
     * @param x private key (1 <= x < n)
     * @param n group order
     * @return deterministic nonce k
     */
    public static BigInteger generateK(BigInteger h1Int, BigInteger x, BigInteger n) {
        int qlen = n.bitLength();
        int rlen = (qlen + 7) / 8;

        // bits2octets: hash mod n, then int2octets
        //BigInteger h1Int = new BigInteger(1, hash);
        BigInteger hashModQ = h1Int.mod(n);
        byte[] hashBytes = int2octets(hashModQ, rlen);

        byte[] xBytes = int2octets(x, rlen);

        // Step: initialize V and K
        byte[] V = new byte[HASH_SIZE];
        Arrays.fill(V, (byte) 0x01);
        byte[] K = new byte[HASH_SIZE];
        Arrays.fill(K, (byte) 0x00);

        // helper to concatenate arrays
        java.util.function.Function<byte[][], byte[]> concat = (arrays) -> {
            int len = 0;
            for (byte[] a : arrays) len += a.length;
            byte[] out = new byte[len];
            int pos = 0;
            for (byte[] a : arrays) {
                System.arraycopy(a, 0, out, pos, a.length);
                pos += a.length;
            }
            return out;
        };

        // K = HMAC(K, V || 0x00 || xBytes || hashBytes)
        K = HMACSHA256.hmac(K, concat.apply(new byte[][]{V, new byte[]{0x00}, xBytes, hashBytes}));
        // V = HMAC(K, V)
        V = HMACSHA256.hmac(K, V);

        // K = HMAC(K, V || 0x01 || xBytes || hashBytes)
        K = HMACSHA256.hmac(K, concat.apply(new byte[][]{V, new byte[]{0x01}, xBytes, hashBytes}));
        // V = HMAC(K, V)
        V = HMACSHA256.hmac(K, V);

        while (true) {
            byte[] T = new byte[0];
            while (T.length < rlen) {
                byte[] vtmp = HMACSHA256.hmac(K, V);
                V = vtmp;
                byte[] tmp = new byte[T.length + vtmp.length];
                System.arraycopy(T, 0, tmp, 0, T.length);
                System.arraycopy(vtmp, 0, tmp, T.length, vtmp.length);
                T = tmp;
            }
            byte[] kBytes = Arrays.copyOfRange(T, 0, rlen);
            BigInteger kCandidate = new BigInteger(1, kBytes);

            if (kCandidate.signum() > 0 && kCandidate.compareTo(n) < 0) {
                return kCandidate;
            }

            // K = HMAC(K, V || 0x00)
            K = HMACSHA256.hmac(K, concat.apply(new byte[][]{V, new byte[]{0x00}}));
            // V = HMAC(K, V)
            V = HMACSHA256.hmac(K, V);
        }
    }

    // int2octets: convert integer to fixed length byte array (big endian)
    private static byte[] int2octets(BigInteger v, int len) {
        byte[] bs = v.toByteArray();
        if (bs.length == len) return bs;
        if (bs.length > len) {
            // Take last len bytes
            return Arrays.copyOfRange(bs, bs.length - len, bs.length);
        }
        // Pad with leading zeros
        byte[] res = new byte[len];
        System.arraycopy(bs, 0, res, len - bs.length, bs.length);
        return res;
    }
}
