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
package com.github.chainfs.ecdnist;

import java.math.BigInteger;

public class NistP521EC {

    // Field prime: p = 2^521 - 1
    public static final BigInteger P = BigInteger.valueOf(2).pow(521).subtract(BigInteger.ONE);

    // Curve order
    public static final BigInteger N = new BigInteger(
            "686479766013060971498190079908139321726943530014330540939446345918554318339765539424505774633321719753" +
            "430986903069123103141175210916864000000000000000000000000000000000000000000000000001", 10);

    // Curve parameters: y^2 = x^3 + ax + b
    public static final BigInteger A = BigInteger.valueOf(-3);
    public static final BigInteger B = new BigInteger(
            "051953EB9618E1C9A1F929A21A0B68540EEA2DA725B99B315F3B8B489918EF109E156193951EC7E937B1652C0BD3BB1BF073573DF883D2C34F1EF451FD46B503F00", 16);

    // Base point G
    public static final BigInteger GX = new BigInteger(
            "C6858E06B70404E9CD9E3ECB662395B4429C648139053FB521F828AF606B4D3DBAA14B5E77EFE75928FE1DC127A2FFA8DE3348B3C1856A429BF97E7E31C2E5BD66", 16);
    public static final BigInteger GY = new BigInteger(
            "11839296A789A3BC0045C8A5FB42C7D1BD998F54449579B446817AFBD17273E662C97EE72995EF42640C550B9013FAD0761353C7086A272C24088BE94769FD16650", 16);

    public static final BigInteger[] G = new BigInteger[]{GX, GY};

    public static BigInteger mod(BigInteger x) {
        x = x.mod(P);
        return x.signum() < 0 ? x.add(P) : x;
    }

    public static boolean isOnCurve(BigInteger x, BigInteger y) {
        BigInteger left = mod(y.pow(2));
        BigInteger right = mod(x.pow(3).add(A.multiply(x)).add(B));
        return left.equals(right);
    }

    public static BigInteger[] pointAdd(BigInteger[] P1, BigInteger[] Q) {
        if (P1 == null) return Q;
        if (Q == null) return P1;

        BigInteger x1 = P1[0], y1 = P1[1];
        BigInteger x2 = Q[0], y2 = Q[1];

        if (x1.equals(x2)) {
            if (!y1.equals(y2)) {
                return null; // Point at infinity
            } else {
                return pointDouble(P1);
            }
        }

        BigInteger lambda = y2.subtract(y1).multiply(x2.subtract(x1).modInverse(P)).mod(P);
        BigInteger x3 = mod(lambda.pow(2).subtract(x1).subtract(x2));
        BigInteger y3 = mod(lambda.multiply(x1.subtract(x3)).subtract(y1));
        return new BigInteger[]{x3, y3};
    }

    public static BigInteger[] pointDouble(BigInteger[] Q) {
        if (Q == null) return null;

        BigInteger x = Q[0], y = Q[1];
        if (y.equals(BigInteger.ZERO)) return null; // Point at infinity

        BigInteger lambda = x.pow(2).multiply(BigInteger.valueOf(3)).add(A)
                .multiply(y.multiply(BigInteger.TWO).modInverse(P)).mod(P);
        BigInteger x3 = mod(lambda.pow(2).subtract(x.multiply(BigInteger.TWO)));
        BigInteger y3 = mod(lambda.multiply(x.subtract(x3)).subtract(y));
        return new BigInteger[]{x3, y3};
    }

    public static BigInteger[] scalarMultiply(BigInteger k, BigInteger[] P) {
        BigInteger[] result = null;
        BigInteger[] addend = P;

        for (int i = k.bitLength() - 1; i >= 0; i--) {
            if (result != null) result = pointDouble(result);
            if (k.testBit(i)) result = pointAdd(result, addend);
        }

        return result;
    }

    public static void printPoint(BigInteger[] point) {
        if (point == null) {
            System.out.println("Point at infinity");
        } else {
            System.out.println("X = " + point[0].toString(16));
            System.out.println("Y = " + point[1].toString(16));
        }
    }
}
