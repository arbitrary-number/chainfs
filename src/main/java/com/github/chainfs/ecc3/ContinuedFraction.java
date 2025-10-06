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
import java.util.ArrayList;
import java.util.List;

public class ContinuedFraction {

    public static List<BigInteger> continuedFraction(BigInteger numerator, BigInteger denominator) {
        List<BigInteger> result = new ArrayList<>();

        while (!denominator.equals(BigInteger.ZERO)) {
            BigInteger[] divMod = numerator.divideAndRemainder(denominator);
            result.add(divMod[0]);  // floor(n/d)
            numerator = denominator;
            denominator = divMod[1];  // remainder
        }

        return result;
    }

    public static void main(String[] args) {
        // Example: π ≈ 355 / 113
        BigInteger n = new BigInteger("355");
        BigInteger d = new BigInteger("113");

        List<BigInteger> cf = continuedFraction(n, d);

        System.out.print("Continued fraction of 355/113: [");
        for (int i = 0; i < cf.size(); i++) {
            System.out.print(cf.get(i));
            if (i < cf.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
    }
}
