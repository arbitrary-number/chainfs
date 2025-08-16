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
 * Copyright (c) Arbitrary Project Team. All rights reserved.
 */
package com.github.chainfs.v2;

public class TaprootAddress {

    public static void main(String[] args) throws Exception {
        // Example 32-byte x-only pubkey (Hex format)
        String pubkeyHex = "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798";
        byte[] pubkeyBytes = hexToBytes(pubkeyHex);

        if (pubkeyBytes.length != 32) {
            throw new IllegalArgumentException("Taproot public key must be 32 bytes (x-only)");
        }

        String address = Bech32m.encode("bc", 0x01, pubkeyBytes);
        System.out.println("Taproot (P2TR) address: " + address);
    }

    // Converts hex string to byte[]
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                  + Character.digit(hex.charAt(i + 1), 16));
        }
        return result;
    }
}
