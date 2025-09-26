package com.github.chainfs.ecc9.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.Secp256k1EC;

import java.math.BigInteger;

public class FractionPropertiesTest {

    // Check: (a/b)G * b == aG
    @Test
    public void testFractionModularInverseIdentity() {
        int a = 7;
        int b = 3;

        // (a/b)G
        BigInteger[] fraction = Secp256k1EC.fraction(a, b);
        // Multiply by b again: ((a/b)G) * b
        BigInteger[] restored = Secp256k1EC.scalarMultiply(BigInteger.valueOf(b), fraction[0], fraction[1]);

        // Compute aG directly
        BigInteger[] expected = Secp256k1EC.scalarMultiply(BigInteger.valueOf(a), Secp256k1EC.GX, Secp256k1EC.GY);

        assertEquals(expected[0], restored[0], "X mismatch in modular inverse check");
        assertEquals(expected[1], restored[1], "Y mismatch in modular inverse check");
    }

    // Check associativity: (a/b + c/d) + e/f == a/b + (c/d + e/f)
    @Test
    public void testFractionAdditionAssociativity() {
        // Use 1/2 + 1/3 + 1/6 = 1
        int[] a = {1, 2};
        int[] b = {1, 3};
        int[] c = {1, 6};

        // ((1/2) + (1/3)) + (1/6)
        BigInteger[] f1 = Secp256k1EC.fraction(a[0], a[1]);
        BigInteger[] f2 = Secp256k1EC.fraction(b[0], b[1]);
        BigInteger[] f3 = Secp256k1EC.fraction(c[0], c[1]);

        BigInteger[] left = Secp256k1EC.add(Secp256k1EC.add(f1, f2), f3);
        BigInteger[] right = Secp256k1EC.add(f1, Secp256k1EC.add(f2, f3));

        assertEquals(left[0], right[0], "X mismatch in associativity check");
        assertEquals(left[1], right[1], "Y mismatch in associativity check");

        // Both should equal 1G
        BigInteger[] oneG = Secp256k1EC.scalarMultiply(BigInteger.ONE, Secp256k1EC.GX, Secp256k1EC.GY);
        assertEquals(oneG[0], left[0], "Left result does not match 1G");
        assertEquals(oneG[1], left[1], "Left result does not match 1G");
    }
}
