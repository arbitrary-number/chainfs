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
package com.github.chainfs.ecc3;
import java.math.BigInteger;

public class ExactMath {

    static class Vector3 {
        BigInteger qx, rx;
        BigInteger qy, ry;
        BigInteger qz, rz;
        BigInteger modulus;

        public Vector3(BigInteger qx, BigInteger rx,
                       BigInteger qy, BigInteger ry,
                       BigInteger qz, BigInteger rz,
                       BigInteger modulus) {
            this.qx = qx; this.rx = rx;
            this.qy = qy; this.ry = ry;
            this.qz = qz; this.rz = rz;
            this.modulus = modulus;
        }

        public BigInteger getX() {
            return qx.multiply(modulus).add(rx);
        }

        public BigInteger getY() {
            return qy.multiply(modulus).add(ry);
        }

        public BigInteger getZ() {
            return qz.multiply(modulus).add(rz);
        }
    }

    public static String exactDistance(Vector3 a, Vector3 b) {
        BigInteger dx = b.getX().subtract(a.getX());
        BigInteger dy = b.getY().subtract(a.getY());
        BigInteger dz = b.getZ().subtract(a.getZ());

        BigInteger dsq = dx.pow(2).add(dy.pow(2)).add(dz.pow(2));

        BigInteger sqrt = integerSqrt(dsq);
        if (sqrt.pow(2).equals(dsq)) {
            return sqrt.toString(); // Perfect square
        } else {
            return "√" + dsq.toString(); // Symbolic
        }
    }

    // Efficient integer square root using binary search
    public static BigInteger integerSqrt(BigInteger n) {
        BigInteger low = BigInteger.ZERO;
        BigInteger high = n;
        BigInteger mid;

        while (low.compareTo(high) <= 0) {
            mid = low.add(high).shiftRight(1);
            BigInteger midSq = mid.multiply(mid);

            int cmp = midSq.compareTo(n);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                low = mid.add(BigInteger.ONE);
            } else {
                high = mid.subtract(BigInteger.ONE);
            }
        }
        return high;
    }

    // Example usage
    public static void main(String[] args) {
        BigInteger n = BigInteger.valueOf(100);

        // A = (102, 205, 99)
        Vector3 a = new Vector3(
            BigInteger.valueOf(1), BigInteger.valueOf(2),
            BigInteger.valueOf(2), BigInteger.valueOf(5),
            BigInteger.ZERO,       BigInteger.valueOf(99),
            n
        );

        // B = (202, 305, 199)
        Vector3 b = new Vector3(
            BigInteger.valueOf(2), BigInteger.valueOf(2),
            BigInteger.valueOf(3), BigInteger.valueOf(5),
            BigInteger.ONE,        BigInteger.valueOf(99),
            n
        );

        String result = exactDistance(a, b);
        System.out.println("Distance: " + result);
    }
}
