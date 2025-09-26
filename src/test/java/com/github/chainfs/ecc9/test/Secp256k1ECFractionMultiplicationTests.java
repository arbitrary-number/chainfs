package com.github.chainfs.ecc9.test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.Secp256k1EC;

public class Secp256k1ECFractionMultiplicationTests {

    @Test
    public void testFractionMultiplicationCommutativity() {
        BigInteger[] G = Secp256k1EC.G;

        int a = 7, b = 9;
        int c = 2, d = 5;

        BigInteger[] left = Secp256k1EC.fractionPoint(Secp256k1EC.fractionPoint(G, a, b), c, d);
        BigInteger[] right = Secp256k1EC.fractionPoint(Secp256k1EC.fractionPoint(G, c, d), a, b);

        assertEquals(0, left[0].compareTo(right[0]), "X coordinates should match");
        assertEquals(0, left[1].compareTo(right[1]), "Y coordinates should match");
    }

    @Test
    public void testFractionMultiplicationIdentity() {
        BigInteger[] G = Secp256k1EC.G;

        BigInteger[] result = Secp256k1EC.fractionPoint(G, 1, 1);

        assertEquals(0, result[0].compareTo(G[0]), "X coordinate should remain the same");
        assertEquals(0, result[1].compareTo(G[1]), "Y coordinate should remain the same");
    }

    @Test
    public void testFractionMultiplicationWithZero() {
        BigInteger[] G = Secp256k1EC.G;

        BigInteger[] result = Secp256k1EC.fractionPoint(G, 0, 1);

        BigInteger[] zero = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };

        assertEquals(0, result[0].compareTo(zero[0]), "X should be 0 (point at infinity)");
        assertEquals(0, result[1].compareTo(zero[1]), "Y should be 0 (point at infinity)");
    }
}
