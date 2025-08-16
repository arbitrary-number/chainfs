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

import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class LegacyBitcoinAddressGenerator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        // Example uncompressed public key (65 bytes, starts with 0x04)
        String pubKeyHex = "04"
            + "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798"
            + "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8";

        byte[] pubKeyBytes = Hex.decode(pubKeyHex);
        String address = generateLegacyAddress(pubKeyBytes);
        System.out.println("Legacy (P2PKH) address: " + address);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String generateUncompressedLegacyAddress(String hex) {
    	byte[] bytes = EncodingUtils.hexToBytes(hex);
    	try {
			return generateLegacyAddress(bytes);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
    }


    public static String generateLegacyAddress(byte[] pubKey) throws Exception {
        // Step 1: SHA-256
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] sha256Hash = sha256.digest(pubKey);

        // Step 2: RIPEMD-160
        MessageDigest ripemd160 = MessageDigest.getInstance("RIPEMD160", "BC");
        byte[] ripemdHash = ripemd160.digest(sha256Hash);

        // Step 3: Add version byte (0x00 for Mainnet)
        byte[] versionedPayload = new byte[ripemdHash.length + 1];
        versionedPayload[0] = 0x00;
        System.arraycopy(ripemdHash, 0, versionedPayload, 1, ripemdHash.length);

        // Step 4: Checksum (first 4 bytes of double SHA-256)
        byte[] checksum = Arrays.copyOfRange(doubleSHA256(versionedPayload), 0, 4);

        // Step 5: Append checksum
        byte[] fullPayload = new byte[versionedPayload.length + 4];
        System.arraycopy(versionedPayload, 0, fullPayload, 0, versionedPayload.length);
        System.arraycopy(checksum, 0, fullPayload, versionedPayload.length, 4);

        // Step 6: Base58 encoding
        return Base58.encode(fullPayload);
    }

    private static byte[] doubleSHA256(byte[] input) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        return sha256.digest(sha256.digest(input));
    }
}
