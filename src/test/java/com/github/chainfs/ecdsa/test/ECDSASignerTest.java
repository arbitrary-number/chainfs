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
package com.github.chainfs.ecdsa.test;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc.ECCurve;
import com.github.chainfs.ecc.ECPoint;
import com.github.chainfs.ecdsa.ECDSASigner;

public class ECDSASignerTest {

    // Sample secp256k1 params (only order and G point here)
    private static final BigInteger n = new BigInteger(
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

    public static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    private static final ECCurve curve = ECCurve.getSecp256k1();

    // Use your ECPoint class with G coordinates:
    private static final ECPoint G =  curve.createPoint(
        new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
        new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
    );

    @Test
    public void testSign() {
        ECDSASigner signer = new ECDSASigner(n, G);

        // Private key d (random valid 256-bit integer < n)
        BigInteger d = new BigInteger(
            "1E99423A4ED27608A15A2616E0A1340C18D5B3BD450796F83357D5B6A5A75E24", 16);

        // Message hash z (sha256 digest of some message, for example)
        BigInteger z = new BigInteger(
            "5F1D3C1A3E2E3D7A55D9E47642F5A7F684C5A60DEEF86B5E6AB0D5F1BBFE4936", 16);

        BigInteger[] signature = signer.sign(d, z);

        BigInteger r = signature[0];
        BigInteger s = signature[1];

        // Check r and s are in valid range
        assertTrue(r.compareTo(BigInteger.ZERO) > 0 && r.compareTo(n) < 0, "r is valid");
        assertTrue(s.compareTo(BigInteger.ZERO) > 0 && s.compareTo(n) < 0, "s is valid");

        // Optionally, print out the signature
        System.out.println("r = " + r.toString(16));
        System.out.println("s = " + s.toString(16));
    }

    public boolean verify(BigInteger z, ECPoint Q, BigInteger r, BigInteger s) {
        // Check r and s are in [1, n-1]
        if (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(n.subtract(BigInteger.ONE)) > 0) return false;
        if (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(n.subtract(BigInteger.ONE)) > 0) return false;

        // Compute w = s^(-1) mod n
        BigInteger w = s.modInverse(n);

        // Compute u1 = z * w mod n
        BigInteger u1 = z.multiply(w).mod(n);

        // Compute u2 = r * w mod n
        BigInteger u2 = r.multiply(w).mod(n);

        // Calculate point: u1*G + u2*Q
        ECPoint point = G.multiply(u1).add(Q.multiply(u2));

        if (point.isInfinity()) return false;

        // Signature is valid if r == x1 mod n where (x1, y1) = point
        BigInteger x1 = point.getX().mod(n);
        return x1.equals(r);
    }

//    @Test
//    public void testSignAndVerify() {
//        ECDSASigner signer = new ECDSASigner(n, G);
//
//        // Private key d
//        BigInteger d = new BigInteger(
//            "1E99423A4ED27608A15A2616E0A1340C18D5B3BD450796F83357D5B6A5A75E24", 16);
//
//        // Public key Q = d * G
//        ECPoint Q = G.multiply(d);
//
//        // Message hash z (sha256 of a message, already provided)
//        BigInteger z = new BigInteger(
//            "5F1D3C1A3E2E3D7A55D9E47642F5A7F684C5A60DEEF86B5E6AB0D5F1BBFE4936", 16);
//
//        // Sign the hash
//        BigInteger[] signature = signer.sign(d, z);
//        BigInteger r = signature[0];
//        BigInteger s = signature[1];
//
//        // Now verify the signature
//        boolean isValid = verify(z, Q, r, s);
//        assertTrue(isValid, "ECDSA signature should verify successfully");
//
//        // Optional print
//        System.out.println("✅ Signature verified!");
//        System.out.println("r = " + r.toString(16));
//        System.out.println("s = " + s.toString(16));
//    }

    @Test
    public void testSignAndVerifyDebug() {
        ECDSASigner signer = new ECDSASigner(n, G);
        BigInteger d = new BigInteger("1E99423A4ED27608A15A2616E0A1340C18D5B3BD450796F83357D5B6A5A75E24", 16);
        ECPoint Q = G.multiply(d);
        BigInteger z = new BigInteger("5F1D3C1A3E2E3D7A55D9E47642F5A7F684C5A60DEEF86B5E6AB0D5F1BBFE4936", 16);

        BigInteger[] sig = signer.sign(d, z);
        BigInteger r = sig[0], s = sig[1];
        System.out.println("Q = " + Q.getX().toString(16) + ", " + Q.getY().toString(16));
        System.out.println("Signature r = " + r.toString(16));
        System.out.println("Signature s = " + s.toString(16));

        // In verify:
        BigInteger w = s.modInverse(n);
        BigInteger u1 = z.multiply(w).mod(n);
        BigInteger u2 = r.multiply(w).mod(n);
        ECPoint P1 = G.multiply(u1);
        ECPoint P2 = Q.multiply(u2);
        ECPoint R = P1.add(P2);

        System.out.println("u1 = " + u1.toString(16));
        System.out.println("u2 = " + u2.toString(16));
        System.out.println("R = " + R.getX().toString(16) + ", " + R.getY().toString(16));
        BigInteger x1_modn = R.getX().mod(n);
        System.out.println("x1 mod n = " + x1_modn.toString(16));

        boolean valid = x1_modn.equals(r);
        System.out.println("verify result = " + valid);

        assertTrue(valid);
    }

    @Test
    public void testSignAndVerifyWithMagic() {
        ECDSASigner signer = new ECDSASigner(n, G);

        BigInteger d = new BigInteger(
            "1E99423A4ED27608A15A2616E0A1340C18D5B3BD450796F83357D5B6A5A75E24", 16); // private key

        BigInteger z = new BigInteger(
            "5F1D3C1A3E2E3D7A55D9E47642F5A7F684C5A60DEEF86B5E6AB0D5F1BBFE4936", 16); // message hash

        // ✍️ Sign the message hash using private key
        System.out.println("\u001B[36m📜 Signing Message...\u001B[0m");
        BigInteger[] sig = signer.sign(d, z);
        BigInteger r = sig[0];
        BigInteger s = sig[1];

        System.out.println("\u001B[35m🔑 Private key (d):\u001B[0m " + d.toString(16));
        System.out.println("\u001B[32m✒️ Signature (r):\u001B[0m " + r.toString(16));
        System.out.println("\u001B[32m✒️ Signature (s):\u001B[0m " + s.toString(16));

        // 🔐 Derive the public key Q = d * G
        ECPoint Q = G.multiply(d);
        System.out.println("\u001B[34m🧙‍♂️ Public key (Q):\u001B[0m " +
            Q.getX().toString(16) + ", " + Q.getY().toString(16));

        // 🧪 Verify the signature
        System.out.println("\u001B[36m🕵️ Verifying signature...\u001B[0m");

        BigInteger w = s.modInverse(n);
        BigInteger u1 = z.multiply(w).mod(n);
        BigInteger u2 = r.multiply(w).mod(n);
        ECPoint R = G.multiply(u1).add(Q.multiply(u2));

        System.out.println("\u001B[33m🧮 u1 =\u001B[0m " + u1.toString(16));
        System.out.println("\u001B[33m🧮 u2 =\u001B[0m " + u2.toString(16));
        System.out.println("\u001B[35m📍 R =\u001B[0m " + R.getX().toString(16) + ", " + R.getY().toString(16));

        BigInteger x1ModN = R.getX().mod(n);
        System.out.println("\u001B[36m🔍 x1 mod n =\u001B[0m " + x1ModN.toString(16));
        boolean result = x1ModN.equals(r);

        if (result) {
            System.out.println("\u001B[32m✅ 🎉 Signature Verified Successfully!\u001B[0m");
        } else {
            System.out.println("\u001B[31m❌ Signature Verification Failed!\u001B[0m");
        }

        assertTrue(result, "Signature should verify successfully");

        // 🧠 Explain the principle
        System.out.println("\n\u001B[36m📘 From First Principles:\u001B[0m");
        System.out.println("1️⃣ A signature (r, s) proves knowledge of the private key without revealing it.");
        System.out.println("2️⃣ 'k' is deterministically generated using RFC 6979 to prevent leakage.");
        System.out.println("3️⃣ Signature equation: s = k⁻¹(z + r·d) mod n");
        System.out.println("4️⃣ Verification computes R = u1·G + u2·Q, then checks R.x ≡ r mod n.");
        System.out.println("5️⃣ 🦄 Magic: No direct knowledge of 'd' is needed to verify — only Q!");
    }


}
