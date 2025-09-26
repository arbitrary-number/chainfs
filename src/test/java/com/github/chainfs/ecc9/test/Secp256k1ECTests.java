package com.github.chainfs.ecc9.test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.Secp256k1EC;

import java.math.BigInteger;

public class Secp256k1ECTests {

    @Test
    public void testAdditiveInverse() {
        BigInteger[] point = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(7, 1));
        BigInteger[] negPoint = Secp256k1EC.negate(point);

        BigInteger[] sum = Secp256k1EC.add(point, negPoint);

        BigInteger[] zeroPoint = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(0, 1));

        assertEquals(0, zeroPoint[0].compareTo(sum[0]), "X should be zero for additive inverse sum");
        assertEquals(0, zeroPoint[1].compareTo(sum[1]), "Y should be zero for additive inverse sum");
    }

    @Test
    public void testCommutativity() {
        BigInteger[] P = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(3, 1));
        BigInteger[] Q = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(5, 1));

        BigInteger[] sumPQ = Secp256k1EC.add(P, Q);
        BigInteger[] sumQP = Secp256k1EC.add(Q, P);

        assertEquals(0, sumPQ[0].compareTo(sumQP[0]), "X coordinates should match for commutativity");
        assertEquals(0, sumPQ[1].compareTo(sumQP[1]), "Y coordinates should match for commutativity");
    }

    @Test
    public void testAssociativity() {
        BigInteger[] P = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(2, 1));
        BigInteger[] Q = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(3, 1));
        BigInteger[] R = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(4, 1));

        BigInteger[] left = Secp256k1EC.add(Secp256k1EC.add(P, Q), R);
        BigInteger[] right = Secp256k1EC.add(P, Secp256k1EC.add(Q, R));

        assertEquals(0, left[0].compareTo(right[0]), "X coordinates should match for associativity");
        assertEquals(0, left[1].compareTo(right[1]), "Y coordinates should match for associativity");
    }

    @Test
    public void testScalarMultiplicationDistributivity() {
        BigInteger[] P = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(2, 1));
        BigInteger[] Q = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(3, 1));
        int k = 5;

        BigInteger[] left = Secp256k1EC.multiply(Secp256k1EC.add(P, Q), Secp256k1EC.fraction(k, 1));
        BigInteger[] right = Secp256k1EC.add(
            Secp256k1EC.multiply(P, Secp256k1EC.fraction(k, 1)),
            Secp256k1EC.multiply(Q, Secp256k1EC.fraction(k, 1))
        );

        assertEquals(0, left[0].compareTo(right[0]), "X coordinates should match for distributivity");
        assertEquals(0, left[1].compareTo(right[1]), "Y coordinates should match for distributivity");
    }

    @Test
    public void testEquivalentFractionsProduceSamePoint() {
        // Use 1/2 and 2/4 - both represent the same fraction
        BigInteger[] point1 = Secp256k1EC.multiply(
            Secp256k1EC.G,
            Secp256k1EC.fraction(1, 2)
        );

        BigInteger[] point2 = Secp256k1EC.multiply(
            Secp256k1EC.G,
            Secp256k1EC.fraction(2, 4)
        );

        // Check that both points are the same
        assertEquals(0, point1[0].compareTo(point2[0]), "X coordinates should match");
        assertEquals(0, point1[1].compareTo(point2[1]), "Y coordinates should match");
    }

    @Test
    public void testFractionMultiplicationAssociativity() {
        // Base point G
        BigInteger[] G = Secp256k1EC.G;

        // Define fractions a/b and c/d
        int a = 3, b = 4;  // 3/4
        int c = 5, d = 6;  // 5/6

        // Compute combined fraction (a/b)*(c/d) = (a*c)/(b*d)
        int ac = a * c;
        int bd = b * d;

        // Left side: ((a/b) * (c/d)) * G
        BigInteger[] left = Secp256k1EC.fractionPoint(G, ac, bd);

        // Right side: (a/b) * ((c/d) * G)
        BigInteger[] temp = Secp256k1EC.fractionPoint(G, c, d);
        BigInteger[] right = Secp256k1EC.fractionPoint(temp, a, b);

        // Assert both points are equal (compare X and Y)
        assertEquals(0, left[0].compareTo(right[0]), "X coordinates should match");
        assertEquals(0, left[1].compareTo(right[1]), "Y coordinates should match");
    }

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
}
