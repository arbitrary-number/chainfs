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

import java.util.Locale;

import org.bouncycastle.jcajce.provider.digest.Keccak;

public class EthereumAddress {

	public static String getEthereumAddress(byte[] pubKeyBytes) {
		// Compute Keccak-256 hash of the public key bytes
	    byte[] hash = keccak256(pubKeyBytes);

	    // Take last 20 bytes for address
	    byte[] addressBytes = new byte[20];
	    System.arraycopy(hash, hash.length - 20, addressBytes, 0, 20);

	    // Convert to hex string and add 0x prefix
	    String address = "0x" + EncodingUtils.bytesToHex(addressBytes);

	    System.out.println("Ethereum address: " + address);
	    return address;
	}

	public static String toChecksumAddress(String address) {
        String cleanAddress = address.toLowerCase(Locale.ROOT).replace("0x", "");

        // Keccak-256 hash of the lowercase address
        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(cleanAddress.getBytes());
        byte[] hashBytes = keccak.digest();
        String hashHex = EncodingUtils.bytesToHex(hashBytes);

        StringBuilder checksummed = new StringBuilder("0x");

        for (int i = 0; i < cleanAddress.length(); i++) {
            char c = cleanAddress.charAt(i);
            int hashNibble = Character.digit(hashHex.charAt(i), 16);
            checksummed.append((hashNibble >= 8) ? Character.toUpperCase(c) : c);
        }

        return checksummed.toString();
    }


	 // Keccak-256 hash function
    public static byte[] keccak256(byte[] input) {
        Keccak.Digest256 digest256 = new Keccak.Digest256();
        digest256.update(input, 0, input.length);
        return digest256.digest();
    }

}
