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

    public static void main(String[] args) {
        // Example congruences:
        // x ≡ 2 mod 3
        // x ≡ 3 mod 5
        // x ≡ 2 mod 7

        BigInteger[] a = {
            BigInteger.valueOf(2),
            BigInteger.valueOf(3),
            BigInteger.valueOf(2)
        };

        BigInteger[] m = {
            BigInteger.valueOf(3),
            BigInteger.valueOf(5),
            BigInteger.valueOf(7)
        };

        BigInteger x = solveCRT(a, m);

        BigInteger M = BigInteger.ONE;
        for (BigInteger mod : m) {
            M = M.multiply(mod);
        }

        System.out.println("Solution: x ≡ " + x + " mod " + M);
    }
}
