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

public class HMACSHA256 {
    private static final int BLOCK_SIZE = 64; // 512 bits = 64 bytes

    // Compute HMAC-SHA256(key, message)
    public static byte[] hmac(byte[] key, byte[] message) {
        if (key.length > BLOCK_SIZE) {
            key = SHA256Hash.sha256(key); // hash long keys first
        }

        // Pad key to BLOCK_SIZE with zeros
        byte[] keyPadded = new byte[BLOCK_SIZE];
        System.arraycopy(key, 0, keyPadded, 0, key.length);

        byte[] oKeyPad = new byte[BLOCK_SIZE];
        byte[] iKeyPad = new byte[BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            oKeyPad[i] = (byte)(keyPadded[i] ^ 0x5c);
            iKeyPad[i] = (byte)(keyPadded[i] ^ 0x36);
        }

        // inner hash = SHA256(iKeyPad || message)
        byte[] innerData = new byte[iKeyPad.length + message.length];
        System.arraycopy(iKeyPad, 0, innerData, 0, iKeyPad.length);
        System.arraycopy(message, 0, innerData, iKeyPad.length, message.length);
        byte[] innerHash = SHA256Hash.sha256(innerData);

        // outer hash = SHA256(oKeyPad || innerHash)
        byte[] outerData = new byte[oKeyPad.length + innerHash.length];
        System.arraycopy(oKeyPad, 0, outerData, 0, oKeyPad.length);
        System.arraycopy(innerHash, 0, outerData, oKeyPad.length, innerHash.length);
        return SHA256Hash.sha256(outerData);
    }
}
