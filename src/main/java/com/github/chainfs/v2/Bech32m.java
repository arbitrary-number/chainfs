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
package com.github.chainfs.v2;

import java.util.Arrays;

public class Bech32m {
    private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
    private static final int[] GENERATOR = {0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3};
    private static final int BECH32M_CONST = 0x2bc830a3;

    public static String encode(String hrp, int witver, byte[] witprog) {
        if (witver < 0 || witver > 16) throw new IllegalArgumentException("Invalid witness version");

        byte[] enc = convertBits(witprog, 8, 5, true);
        byte[] data = new byte[1 + enc.length];
        data[0] = (byte) witver;
        System.arraycopy(enc, 0, data, 1, enc.length);
        return encodeBech32m(hrp, data);
    }

    private static String encodeBech32m(String hrp, byte[] data) {
        byte[] checksum = createChecksum(hrp, data);
        byte[] combined = new byte[data.length + checksum.length];
        System.arraycopy(data, 0, combined, 0, data.length);
        System.arraycopy(checksum, 0, combined, data.length, checksum.length);

        StringBuilder sb = new StringBuilder(hrp.length() + 1 + combined.length);
        sb.append(hrp).append('1');
        for (byte b : combined) {
            sb.append(CHARSET.charAt(b));
        }
        return sb.toString();
    }

    private static byte[] createChecksum(String hrp, byte[] data) {
        byte[] values = new byte[expandHrp(hrp).length + data.length + 6];
        System.arraycopy(expandHrp(hrp), 0, values, 0, expandHrp(hrp).length);
        System.arraycopy(data, 0, values, expandHrp(hrp).length, data.length);
        Arrays.fill(values, expandHrp(hrp).length + data.length, values.length, (byte) 0);
        int mod = polymod(values) ^ BECH32M_CONST;

        byte[] ret = new byte[6];
        for (int i = 0; i < 6; ++i) {
            ret[i] = (byte) ((mod >> (5 * (5 - i))) & 31);
        }
        return ret;
    }

    private static int polymod(byte[] values) {
        int chk = 1;
        for (byte v : values) {
            int b = (chk >> 25) & 0xff;
            chk = ((chk & 0x1ffffff) << 5) ^ (v & 0xff);
            for (int i = 0; i < 5; i++) {
                chk ^= ((b >> i) & 1) == 1 ? GENERATOR[i] : 0;
            }
        }
        return chk;
    }

    private static byte[] expandHrp(String hrp) {
        byte[] ret = new byte[hrp.length() * 2 + 1];
        for (int i = 0; i < hrp.length(); ++i) {
            ret[i] = (byte) (hrp.charAt(i) >> 5);
        }
        ret[hrp.length()] = 0;
        for (int i = 0; i < hrp.length(); ++i) {
            ret[hrp.length() + 1 + i] = (byte) (hrp.charAt(i) & 31);
        }
        return ret;
    }

    private static byte[] convertBits(byte[] data, int fromBits, int toBits, boolean pad) {
        int acc = 0;
        int bits = 0;
        int maxv = (1 << toBits) - 1;
        byte[] result = new byte[(data.length * fromBits + toBits - 1) / toBits];
        int index = 0;

        for (byte value : data) {
            int b = value & 0xFF;
            if ((b >> fromBits) > 0) {
                throw new IllegalArgumentException("Invalid data");
            }
            acc = (acc << fromBits) | b;
            bits += fromBits;
            while (bits >= toBits) {
                bits -= toBits;
                result[index++] = (byte) ((acc >> bits) & maxv);
            }
        }

        if (pad) {
            if (bits > 0) {
                result[index++] = (byte) ((acc << (toBits - bits)) & maxv);
            }
        } else {
            if (bits >= fromBits || ((acc << (toBits - bits)) & maxv) != 0) {
                throw new IllegalArgumentException("Invalid padding in data");
            }
        }

        return Arrays.copyOfRange(result, 0, index);
    }
}
