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
package com.github.chainfs.ecc4;
import java.math.BigInteger;

public class PiConvergence {

    // Fraction class for rational arithmetic
    static class Fraction {
        BigInteger numerator;
        BigInteger denominator;

        Fraction(long n, long d) {
            numerator = BigInteger.valueOf(n);
            denominator = BigInteger.valueOf(d);
            normalize();
        }

        Fraction(BigInteger n, BigInteger d) {
            numerator = n;
            denominator = d;
            normalize();
        }

        private void normalize() {
            BigInteger gcd = numerator.gcd(denominator);
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
            if (denominator.signum() < 0) {
                numerator = numerator.negate();
                denominator = denominator.negate();
            }
        }

        Fraction add(Fraction other) {
            BigInteger num = numerator.multiply(other.denominator).add(other.numerator.multiply(denominator));
            BigInteger den = denominator.multiply(other.denominator);
            return new Fraction(num, den);
        }

        Fraction subtract(Fraction other) {
            BigInteger num = numerator.multiply(other.denominator).subtract(other.numerator.multiply(denominator));
            BigInteger den = denominator.multiply(other.denominator);
            return new Fraction(num, den);
        }

        Fraction multiply(Fraction other) {
            return new Fraction(numerator.multiply(other.numerator), denominator.multiply(other.denominator));
        }

        Fraction divide(Fraction other) {
            return new Fraction(numerator.multiply(other.denominator), denominator.multiply(other.numerator));
        }

        @Override
        public String toString() {
            return numerator + "/" + denominator + " ≈ " + toDecimal(50);
        }

        // Approximate decimal representation with scale digits after decimal point
        public String toDecimal(int scale) {
            java.math.BigDecimal num = new java.math.BigDecimal(numerator);
            java.math.BigDecimal den = new java.math.BigDecimal(denominator);
            java.math.BigDecimal result = num.divide(den, scale, java.math.RoundingMode.HALF_UP);
            return result.toPlainString();
        }
    }

    public static void main(String[] args) {
        // Leibniz series: pi/4 = 1 - 1/3 + 1/5 - 1/7 + ...
        Fraction piOver4 = new Fraction(0, 1);

        int terms = 30;  // Number of terms in the series

        for (int k = 0; k < terms; k++) {
            Fraction term = new Fraction(1, 2 * k + 1);
            if (k % 2 == 1) {
                term = term.multiply(new Fraction(-1, 1));  // alternate signs
            }
            piOver4 = piOver4.add(term);
        }

        Fraction piApprox = piOver4.multiply(new Fraction(4, 1));

        System.out.println("Approximation of π after " + terms + " terms:");
        System.out.println(piApprox);
        System.out.println("Decimal approximation: " + piApprox.toDecimal(50));
    }
}
