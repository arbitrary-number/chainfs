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
package com.github.chainfs.ecc;

import java.math.BigInteger;

public class ECCurve {
    private final BigInteger p;  // prime modulus
    private final BigInteger a;
    private final BigInteger b;

    private static ECCurve secp256k1;

    public ECCurve(BigInteger p, BigInteger a, BigInteger b) {
        this.p = p;
        this.a = a;
        this.b = b;
    }

    public static ECCurve getSecp256k1() {
        if (secp256k1 == null) {
            BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
            BigInteger a = BigInteger.ZERO;
            BigInteger b = BigInteger.valueOf(7);
            secp256k1 = new ECCurve(p, a, b);
        }
        return secp256k1;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    public ECPoint createPoint(BigInteger x, BigInteger y) {
        ECPoint point = new ECPoint(x, y, this);
        if (!point.isOnCurve()) {
            throw new IllegalArgumentException("Point is not on the curve");
        }
        return point;
    }

    public ECPoint createInterpolatedPoint(BigInteger x, BigInteger y) {
        ECPoint point = new ECPoint(x, y, this);
        return point;
    }

    public ECPoint getInfinity() {
        return new ECPoint(null, null, this); // represents point at infinity
    }
}
