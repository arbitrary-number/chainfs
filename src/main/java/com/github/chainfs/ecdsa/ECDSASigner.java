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

import com.github.chainfs.ecc.ECPoint;

public class ECDSASigner {

    private final BigInteger n;  // order of the curve
    private final ECPoint G;     // base point
    private final RFC6979Nonce rfc6979;

    public ECDSASigner(BigInteger n, ECPoint G) {
        this.n = n;
        this.G = G;
        this.rfc6979 = new RFC6979Nonce();
    }

    // sign message hash z with private key d
    public BigInteger[] sign(BigInteger d, BigInteger z) {
        BigInteger k = RFC6979Nonce.generateK(z, d, n);

        // R = k * G
        ECPoint R = G.multiply(k);
        BigInteger r = R.getX().mod(n);
        if (r.equals(BigInteger.ZERO)) {
            throw new RuntimeException("r is zero, try again");
        }

        BigInteger kInv = k.modInverse(n);
        BigInteger s = kInv.multiply(z.add(r.multiply(d))).mod(n);
        if (s.equals(BigInteger.ZERO)) {
            throw new RuntimeException("s is zero, try again");
        }
        System.out.println("Sign: k = " + k.toString(16));
        System.out.println("Sign: R.x = " + R.getX().toString(16));
        System.out.println("Sign: r = " + r.toString(16));

        return new BigInteger[]{r, s};
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

}
