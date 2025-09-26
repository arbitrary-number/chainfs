package com.github.chainfs.ecc9.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.Secp256k1EC;

public class FractionPropertiesSuite {

    @Test
    public void testFractionModularIdentity() {
        int a = 7;
        int b = 3;

        BigInteger[] fraction = Secp256k1EC.fraction(a, b); // (a/b)G
        BigInteger[] redoubled = Secp256k1EC.scalarMultiply(BigInteger.valueOf(b), fraction[0], fraction[1]); // ⋅b

        BigInteger[] expected = Secp256k1EC.scalarMultiply(BigInteger.valueOf(a), Secp256k1EC.GX, Secp256k1EC.GY);

        assertEquals(expected[0], redoubled[0], "X mismatch: (a/b)G * b != aG");
        assertEquals(expected[1], redoubled[1], "Y mismatch: (a/b)G * b != aG");
    }

    @Test
    public void testFractionAssociativity() {
        // Example: (1/2 + 1/3) + 1/6 == 1/2 + (1/3 + 1/6)
        int[] a = {1, 2};
        int[] b = {1, 3};
        int[] c = {1, 6};

        BigInteger[] fa = Secp256k1EC.fraction(a[0], a[1]);
        BigInteger[] fb = Secp256k1EC.fraction(b[0], b[1]);
        BigInteger[] fc = Secp256k1EC.fraction(c[0], c[1]);

        BigInteger[] left = Secp256k1EC.add(Secp256k1EC.add(fa, fb), fc);
        BigInteger[] right = Secp256k1EC.add(fa, Secp256k1EC.add(fb, fc));

        assertEquals(left[0], right[0], "X mismatch: associativity fails");
        assertEquals(left[1], right[1], "Y mismatch: associativity fails");

        // Sanity check: result == G
        BigInteger[] oneG = Secp256k1EC.scalarMultiply(BigInteger.ONE, Secp256k1EC.GX, Secp256k1EC.GY);
        assertEquals(oneG[0], left[0], "Associative result ≠ G");
        assertEquals(oneG[1], left[1], "Associative result ≠ G");
    }

    @Test
    public void testFractionCommutativity() {
        // Test: 3/5 + 2/7 == 2/7 + 3/5
        int[] a = {3, 5};
        int[] b = {2, 7};

        BigInteger[] fa = Secp256k1EC.fraction(a[0], a[1]);
        BigInteger[] fb = Secp256k1EC.fraction(b[0], b[1]);

        BigInteger[] sumAB = Secp256k1EC.add(fa, fb);
        BigInteger[] sumBA = Secp256k1EC.add(fb, fa);

        assertEquals(sumAB[0], sumBA[0], "X mismatch: a + b ≠ b + a");
        assertEquals(sumAB[1], sumBA[1], "Y mismatch: a + b ≠ b + a");
    }
}
