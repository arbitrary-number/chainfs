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

import java.security.MessageDigest;
import java.security.Security;
import java.util.Arrays;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class NestedSegwitAddress {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // Example compressed public key (33 bytes)
        String pubKeyHex = "0279BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798";
        byte[] pubKey = Hex.decode(pubKeyHex);

        String address = generateNestedSegwitAddress(pubKey);
        System.out.println("Nested SegWit (P2SH-P2WPKH) Address: " + address);
    }

    public static String generateNestedSegwitAddress(byte[] compressedPubKey) throws Exception {
        if (compressedPubKey.length != 33 || (compressedPubKey[0] != 0x02 && compressedPubKey[0] != 0x03)) {
            throw new IllegalArgumentException("Public key must be compressed (33 bytes, starts with 0x02 or 0x03)");
        }

        // Step 1: HASH160(pubKey) = RIPEMD160(SHA256(pubKey))
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] sha256Hash = sha256.digest(compressedPubKey);

        MessageDigest ripemd160 = MessageDigest.getInstance("RIPEMD160", "BC");
        byte[] pubKeyHash = ripemd160.digest(sha256Hash);

        // Step 2: Build redeem script: 0x00 0x14 <pubKeyHash>
        byte[] redeemScript = new byte[2 + pubKeyHash.length];
        redeemScript[0] = 0x00;          // OP_0
        redeemScript[1] = 0x14;          // PUSH 20 bytes
        System.arraycopy(pubKeyHash, 0, redeemScript, 2, pubKeyHash.length);

        // Step 3: HASH160(redeemScript)
        byte[] redeemHash = ripemd160.digest(sha256.digest(redeemScript));

        // Step 4: Build P2SH address: Base58Check(0x05 + redeemHash)
        byte[] p2shPayload = new byte[1 + redeemHash.length];
        p2shPayload[0] = 0x05;  // Version byte for P2SH (mainnet)
        System.arraycopy(redeemHash, 0, p2shPayload, 1, redeemHash.length);

        byte[] checksum = Arrays.copyOfRange(doubleSHA256(p2shPayload), 0, 4);

        byte[] full = new byte[p2shPayload.length + 4];
        System.arraycopy(p2shPayload, 0, full, 0, p2shPayload.length);
        System.arraycopy(checksum, 0, full, p2shPayload.length, 4);

        return Base58.encode(full);
    }

    private static byte[] doubleSHA256(byte[] input) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        return sha256.digest(sha256.digest(input));
    }
}
