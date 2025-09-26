package com.github.chainfs.ecc9.test;
import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.Secp256k1EC;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PointAtInfinityTest {

    private static final BigInteger[] POINT_AT_INFINITY = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };

    @Test
    public void testAddPointAtInfinityToPoint() {
        // Pick a random point, say G (generator)
        BigInteger[] G = Secp256k1EC.G;

        // Add point at infinity to G
        BigInteger[] sum1 = Secp256k1EC.add(G, POINT_AT_INFINITY);
        BigInteger[] sum2 = Secp256k1EC.add(POINT_AT_INFINITY, G);

        // Both sums should equal G
        assertEquals(0, sum1[0].compareTo(G[0]), "X coordinate should be unchanged");
        assertEquals(0, sum1[1].compareTo(G[1]), "Y coordinate should be unchanged");

        assertEquals(0, sum2[0].compareTo(G[0]), "X coordinate should be unchanged");
        assertEquals(0, sum2[1].compareTo(G[1]), "Y coordinate should be unchanged");
    }

    @Test
    public void testMultiplyPointAtInfinityByScalar() {
        BigInteger[] result1 = Secp256k1EC.multiply(POINT_AT_INFINITY, Secp256k1EC.fraction(5, 1));
        BigInteger[] result2 = Secp256k1EC.multiply(POINT_AT_INFINITY, Secp256k1EC.fraction(-3, 4));
        BigInteger[] result3 = Secp256k1EC.multiply(POINT_AT_INFINITY, Secp256k1EC.fraction(0, 1));

        // Multiplying point at infinity by any scalar returns point at infinity
        for (BigInteger[] res : new BigInteger[][] { result1, result2, result3 }) {
            assertEquals(0, res[0].compareTo(POINT_AT_INFINITY[0]), "X coordinate should be 0 (point at infinity)");
            assertEquals(0, res[1].compareTo(POINT_AT_INFINITY[1]), "Y coordinate should be 0 (point at infinity)");
        }
    }
}
