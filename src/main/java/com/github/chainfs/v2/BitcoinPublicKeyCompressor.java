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

import java.math.BigInteger;
import java.util.Arrays;

public class BitcoinPublicKeyCompressor {

	public static String compressPublicKey(String hexString) {
		return EncodingUtils.bytesToHex(
				compressPublicKey(
				EncodingUtils.hexToBytes(hexString)));
	}

    public static byte[] compressPublicKey(byte[] uncompressedPubKey) {
        if (uncompressedPubKey.length != 65 || uncompressedPubKey[0] != 0x04) {
            throw new IllegalArgumentException("Invalid uncompressed public key.");
        }

        byte[] x = Arrays.copyOfRange(uncompressedPubKey, 1, 33);
        byte[] y = Arrays.copyOfRange(uncompressedPubKey, 33, 65);

        BigInteger yInt = new BigInteger(1, y);
        byte prefix = (yInt.testBit(0)) ? (byte) 0x03 : (byte) 0x02;

        byte[] compressed = new byte[33];
        compressed[0] = prefix;
        System.arraycopy(x, 0, compressed, 1, 32);
        return compressed;
    }
}