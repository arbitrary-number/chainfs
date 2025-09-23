package com.github.chainfs.ecc;

import java.math.BigInteger;

public class ChineseRemainderTheorem {

    // Compute the modular inverse of a modulo m using BigInteger
    public static BigInteger modInverse(BigInteger a, BigInteger m) {
        return a.modInverse(m);
    }

    // Apply the CRT algorithm
    public static BigInteger solveCRT(BigInteger[] a, BigInteger[] m) {
        // Compute M = product of all moduli
        BigInteger M = BigInteger.ONE;
        for (BigInteger modulus : m) {
            M = M.multiply(modulus);
        }

        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < a.length; i++) {
            BigInteger Mi = M.divide(m[i]);
            BigInteger yi = modInverse(Mi, m[i]);
            result = result.add(a[i].multiply(Mi).multiply(yi));
        }

        return result.mod(M);
    }

    // Normalize CRT solution to range [-bound, bound]
    public static BigInteger normalizeToHasseBound(BigInteger x, BigInteger M, BigInteger bound) {
        // If x > bound, subtract M to bring it into the signed interval
        if (x.compareTo(bound) > 0) {
            x = x.subtract(M);
        }
        return x;
    }

    public static void main(String[] args) {
        compute(null, null);
    }

    public static void compute(BigInteger[] a, BigInteger[] m) {
        // Example congruences (can replace these with your data):
        if (a == null) {
            a = new BigInteger[] {
                BigInteger.valueOf(2),
                BigInteger.valueOf(3),
                BigInteger.valueOf(2)
            };
        }

        if (m == null) {
            m = new BigInteger[] {
                BigInteger.valueOf(3),
                BigInteger.valueOf(5),
                BigInteger.valueOf(7)
            };
        }

        BigInteger x = solveCRT(a, m);

        BigInteger M = BigInteger.ONE;
        for (BigInteger mod : m) {
            M = M.multiply(mod);
        }

        // secp256k1 prime p = 2^256 - 2^32 - 977
        BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

        // Compute 2 * sqrt(p) as Hasse bound approximation
        // sqrt(p) ~ 2^(256/2) = 2^128, but more exact:
        BigInteger sqrtP = bigIntSqRootFloor(p);
        BigInteger hasseBound = sqrtP.multiply(BigInteger.valueOf(2));

        BigInteger tNormalized = normalizeToHasseBound(x, M, hasseBound);

        System.out.println("CRT solution (mod M): x ≡ " + x.toString(16));
        System.out.println("Product of moduli M: " + M.toString(16));
        System.out.println("Hasse bound (2*sqrt(p)): " + hasseBound.toString(16));
        System.out.println("Normalized t in [-2*sqrt(p), 2*sqrt(p)]: " + tNormalized.toString(16));

        BigInteger expectedT = new BigInteger("14551231950b75fc4402da1722fc9baef", 16);
        System.out.println("Expected t for secp256k1: " + expectedT.toString(16));

        if (tNormalized.equals(expectedT)) {
            System.out.println("Success: Normalized CRT solution matches expected t.");
        } else {
            System.out.println("Warning: Normalized CRT solution does NOT match expected t.");
        }

        BigInteger negT = M.subtract(tNormalized);
        System.out.println("Neg T = " + negT.toString(16));
        if (negT.equals(expectedT)) {
            System.out.println("Success: Negated normalized CRT solution matches expected t.");
        } else {
            System.out.println("Warning: Negated normalized CRT solution does NOT match expected t.");
        }
    }

    // Integer square root floor method (from: https://stackoverflow.com/a/11962756)
    public static BigInteger bigIntSqRootFloor(BigInteger x) {
        BigInteger right = x;
        BigInteger left = BigInteger.ZERO;
        BigInteger mid;

        while (right.subtract(left).compareTo(BigInteger.ONE) > 0) {
            mid = left.add(right).shiftRight(1);
            if (mid.multiply(mid).compareTo(x) > 0) {
                right = mid;
            } else {
                left = mid;
            }
        }
        return left;
    }
}
