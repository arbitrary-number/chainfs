package com.github.chainfs.ecc9.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.Secp256k1EC;

public class FractionEdgeTests {

    private static final SecureRandom random = new SecureRandom();
    private static final BigInteger N = Secp256k1EC.CURVE_ORDER;

    // ---------------------------
    // 1. Negative Fraction Tests
    // ---------------------------

    @Test
    public void testNegativeFractionEqualsNegatedPoint() {
        int a = 3;
        int b = 5;

        BigInteger[] pos = Secp256k1EC.fraction(a, b);          // (3/5)G
        BigInteger[] neg = Secp256k1EC.fraction(-a, b);         // (-3/5)G
        BigInteger[] negOfPos = Secp256k1EC.pointNegateAffine(pos[0], pos[1]);

        assertEquals(negOfPos[0], neg[0], "Negative X mismatch");
        assertEquals(negOfPos[1], neg[1], "Negative Y mismatch");
    }

    @Test
    public void testAddingNegativeCancelsOut() {
        int a = 7;
        int b = 11;

        BigInteger[] pos = Secp256k1EC.fraction(a, b);           // (7/11)G
        BigInteger[] neg = Secp256k1EC.fraction(-a, b);          // (-7/11)G
        BigInteger[] sum = Secp256k1EC.add(pos, neg);            // Should be point at infinity

        BigInteger[] zero = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };
        assertEquals(zero[0], sum[0], "X should be 0 (point at infinity)");
        assertEquals(zero[1], sum[1], "Y should be 0 (point at infinity)");
    }

    @Test
    public void testAddingNegativeCancelsOutOneHalf() {
        int a = 1;
        int b = 2;

        BigInteger[] pos = Secp256k1EC.fraction(a, b);           // (1/2)G
        BigInteger[] neg = Secp256k1EC.fraction(-a, b);          // (-1/2)G
        BigInteger[] sum = Secp256k1EC.add(pos, neg);            // Should be point at infinity

        BigInteger[] zero = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };
        assertEquals(zero[0], sum[0], "X should be 0 (point at infinity)");
        assertEquals(zero[1], sum[1], "Y should be 0 (point at infinity)");
    }

    @Test
    public void testAddingNegativeCancelsOutThreeHalves() {
        int a = 3;
        int b = 2;

        BigInteger[] pos = Secp256k1EC.fraction(a, b);           // (3/2)G
        BigInteger[] neg = Secp256k1EC.fraction(-a, b);          // (-3/2)G
        BigInteger[] sum = Secp256k1EC.add(pos, neg);            // Should be point at infinity

        BigInteger[] zero = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };
        assertEquals(zero[0], sum[0], "X should be 0 (point at infinity)");
        assertEquals(zero[1], sum[1], "Y should be 0 (point at infinity)");
    }


    // -------------------------------------
    // 2. Randomized Fraction Property Tests
    // -------------------------------------

    @Test
    public void testRandomFractionMultiplicationInverseIdentity() {
        for (int i = 0; i < 10; i++) {
            BigInteger a = randScalar();
            BigInteger b = randScalar();

            // Skip if b has no inverse
            if (b.gcd(N).compareTo(BigInteger.ONE) != 0) continue;

            BigInteger[] frac = Secp256k1EC.sFraction(a.toString(), b.toString()); // (a/b)G
            BigInteger[] redoubled = Secp256k1EC.scalarMultiply(b, frac[0], frac[1]); // ⋅b
            BigInteger[] expected = Secp256k1EC.scalarMultiply(a, Secp256k1EC.GX, Secp256k1EC.GY);

            assertEquals(expected[0], redoubled[0], "Random modular inverse test failed: X mismatch");
            assertEquals(expected[1], redoubled[1], "Random modular inverse test failed: Y mismatch");
        }
    }

    @Test
    public void testRandomNegationConsistency() {
        for (int i = 0; i < 10; i++) {
            BigInteger a = randScalar();
            BigInteger b = randScalar();

            if (b.gcd(N).compareTo(BigInteger.ONE) != 0) continue;

            BigInteger[] pos = Secp256k1EC.sFraction(a.toString(), b.toString());
            BigInteger[] neg = Secp256k1EC.sFraction(a.negate().toString(), b.toString());
            BigInteger[] negOfPos = Secp256k1EC.pointNegateAffine(pos[0], pos[1]);

            assertEquals(negOfPos[0], neg[0], "Random negation X mismatch");
            assertEquals(negOfPos[1], neg[1], "Random negation Y mismatch");
        }
    }

    @Test
    public void testHalfGNegation() {
        int a = 1;
        int b = 2;

        BigInteger[] halfG = Secp256k1EC.fraction(a, b);         // (1/2)G
        BigInteger[] negHalfG = Secp256k1EC.fraction(-a, b);     // (-1/2)G
        BigInteger[] negOfHalfG = Secp256k1EC.pointNegateAffine(halfG[0], halfG[1]);

        // Assert that -(1/2)G == (-1/2)G
        assertEquals(negOfHalfG[0], negHalfG[0], "Negation X mismatch for 1/2 G");
        assertEquals(negOfHalfG[1], negHalfG[1], "Negation Y mismatch for 1/2 G");

        // Check that (1/2)G + (-1/2)G = ∞
        BigInteger[] sum = Secp256k1EC.add(halfG, negHalfG);
        BigInteger[] zero = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };

        assertEquals(zero[0], sum[0], "1/2G + (-1/2)G X ≠ 0");
        assertEquals(zero[1], sum[1], "1/2G + (-1/2)G Y ≠ 0");
    }

    @Test
    public void testThreeHalvesGNegation() {
        int a = 3;
        int b = 2;

        BigInteger[] pos = Secp256k1EC.fraction(a, b);       // (3/2)G
        BigInteger[] neg = Secp256k1EC.fraction(-a, b);      // (-3/2)G
        BigInteger[] negOfPos = Secp256k1EC.pointNegateAffine(pos[0], pos[1]);

        // Assert that -(3/2)G == (-3/2)G
        assertEquals(negOfPos[0], neg[0], "Negation X mismatch for 3/2 G");
        assertEquals(negOfPos[1], neg[1], "Negation Y mismatch for 3/2 G");

        // Check that (3/2)G + (-3/2)G = ∞
        BigInteger[] sum = Secp256k1EC.add(pos, neg);
        BigInteger[] zero = new BigInteger[]{ BigInteger.ZERO, BigInteger.ZERO };

        assertEquals(zero[0], sum[0], "3/2G + (-3/2)G X ≠ 0");
        assertEquals(zero[1], sum[1], "3/2G + (-3/2)G Y ≠ 0");
    }

    @Test
    public void testOneThirdGNegation() {
        int a = 1;
        int b = 3;

        BigInteger[] pos = Secp256k1EC.fraction(a, b);       // (1/3)G
        BigInteger[] neg = Secp256k1EC.fraction(-a, b);      // (-1/3)G
        BigInteger[] negOfPos = Secp256k1EC.pointNegateAffine(pos[0], pos[1]);

        // Assert that -(1/3)G == (-1/3)G
        assertEquals(negOfPos[0], neg[0], "Negation X mismatch for 1/3 G");
        assertEquals(negOfPos[1], neg[1], "Negation Y mismatch for 1/3 G");

        // Check that (1/3)G + (-1/3)G = ∞
        BigInteger[] sum = Secp256k1EC.add(pos, neg);
        BigInteger[] zero = new BigInteger[]{ BigInteger.ZERO, BigInteger.ZERO };

        assertEquals(zero[0], sum[0], "1/3G + (-1/3)G X ≠ 0");
        assertEquals(zero[1], sum[1], "1/3G + (-1/3)G Y ≠ 0");
    }

    @Test
    public void testAddingNegativeCancelsOutThreeHalvesOfPoint8G() {
        // Get 8G first
        BigInteger[] point8G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(8,1));

        // Multiply 8G by 3/2
        BigInteger[] pos = Secp256k1EC.fractionPoint(point8G, 3, 2);

        // Multiply 8G by -3/2
        BigInteger[] neg = Secp256k1EC.fractionPoint(point8G, -3, 2);

        // Add the two points
        BigInteger[] sum = Secp256k1EC.add(pos, neg);

        // Point at infinity representation
        BigInteger[] zero = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };

        assertEquals(zero[0], sum[0], "X should be 0 (point at infinity)");
        assertEquals(zero[1], sum[1], "Y should be 0 (point at infinity)");
    }

    @Test
    public void testFractionalMultiplesOfPoint8GAreNegations() {
        // Get 8G first
        BigInteger[] point8G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(8, 1));

        // Get -8G (negation of 8G)
        BigInteger[] negPoint8G = Secp256k1EC.negate(point8G);

        // Multiply 8G by 3/2
        BigInteger[] pos = Secp256k1EC.fractionPoint(point8G, 3, 2);

        // Multiply -8G by 3/2
        BigInteger[] neg = Secp256k1EC.fractionPoint(negPoint8G, 3, 2);

        // Now pos and neg should be negations of each other
        assertEquals(pos[0], neg[0], "X coordinates should match");
        assertEquals(pos[1].negate().mod(Secp256k1EC.P), neg[1].mod(Secp256k1EC.P), "Y coordinates should be negatives mod P");
    }

    @Test
    public void testTripleOfHalf9GAndNegativeHalf9GAreNegations() {
        // Get 9G
        BigInteger[] point9G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(9, 1));

        // Get -9G (negation of 9G)
        BigInteger[] negPoint9G = Secp256k1EC.negate(point9G);

        // Halve 9G
        BigInteger[] half9G = Secp256k1EC.fractionPoint(point9G, 1, 2);

        // Halve -9G
        BigInteger[] halfNeg9G = Secp256k1EC.fractionPoint(negPoint9G, 1, 2);

        // Multiply half9G by 3
        BigInteger[] tripleHalf9G = Secp256k1EC.fractionPoint(half9G, 3, 1);

        // Multiply halfNeg9G by 3
        BigInteger[] tripleHalfNeg9G = Secp256k1EC.fractionPoint(halfNeg9G, 3, 1);

        // Check X coordinates are equal
        assertEquals(tripleHalf9G[0], tripleHalfNeg9G[0], "X coordinates should match");

        // Check Y coordinates are negatives mod P
        assertEquals(
            tripleHalf9G[1].negate().mod(Secp256k1EC.P),
            tripleHalfNeg9G[1].mod(Secp256k1EC.P),
            "Y coordinates should be negatives mod P"
        );
    }

    @Test
    public void testFractionalMultiplyThreeHalvesOf9GAndNegative9GAreNegations() {
        // Get 9G
        BigInteger[] point9G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(9, 1));

        // Get -9G (negation of 9G)
        BigInteger[] negPoint9G = Secp256k1EC.negate(point9G);

        // Multiply 9G by 3/2
        BigInteger[] tripleHalf9G = Secp256k1EC.fractionPoint(point9G, 3, 2);

        // Multiply -9G by 3/2
        BigInteger[] tripleHalfNeg9G = Secp256k1EC.fractionPoint(negPoint9G, 3, 2);

        // Check X coordinates are equal
        assertEquals(tripleHalf9G[0], tripleHalfNeg9G[0], "X coordinates should match");

        // Check X coordinates are equal
        assertTrue(tripleHalf9G[0].compareTo(tripleHalfNeg9G[0]) == 0, "X coordinates should match");

        // Check Y coordinates are negatives mod P
        assertTrue(
            tripleHalf9G[1].negate().mod(Secp256k1EC.P).compareTo(tripleHalfNeg9G[1].mod(Secp256k1EC.P)) == 0,
            "Y coordinates should be negatives mod P"
        );
    }

    @Test
    public void testFractionalMultiplyThreeHalvesAndNegativeThreeHalvesOf9G() {
        // Get 9G
        BigInteger[] point9G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(9, 1));

        // Multiply 9G by 3/2
        BigInteger[] tripleHalf9G = Secp256k1EC.fractionPoint(point9G, 3, 2);

        // Multiply 9G by -3/2 (negative fraction)
        BigInteger[] negTripleHalf9G = Secp256k1EC.fractionPoint(point9G, -3, 2);

        // Check X coordinates are equal
        assertEquals(tripleHalf9G[0], negTripleHalf9G[0], "X coordinates should match");

        // Check Y coordinates are negatives mod P
        assertTrue(
            tripleHalf9G[1].negate().mod(Secp256k1EC.P).compareTo(negTripleHalf9G[1].mod(Secp256k1EC.P)) == 0,
            "Y coordinates should be negatives mod P"
        );
    }

    @Test
    public void testFractionalMultiplyThreeHalvesAndNegativeThreeHalvesOf8G() {
        // Get 9G
        BigInteger[] point8G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(8, 1));

        // Multiply 8G by 3/2
        BigInteger[] tripleHalf8G = Secp256k1EC.fractionPoint(point8G, 3, 2);

        // Multiply 8G by -3/2 (negative fraction)
        BigInteger[] negTripleHalf8G = Secp256k1EC.fractionPoint(point8G, -3, 2);

        // Check X coordinates are equal
        assertEquals(tripleHalf8G[0], negTripleHalf8G[0], "X coordinates should match");

        // Check Y coordinates are negatives mod P
        assertTrue(
            tripleHalf8G[1].negate().mod(Secp256k1EC.P).compareTo(negTripleHalf8G[1].mod(Secp256k1EC.P)) == 0,
            "Y coordinates should be negatives mod P"
        );
    }

    @Test
    public void testFractionalMultiplyThreeHalvesEqualsThreeThenHalf() {
        // Get 8G
        BigInteger[] point8G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(8, 1));

        // Multiply 8G by 3/2
        BigInteger[] directFraction = Secp256k1EC.fractionPoint(point8G, 3, 2);

        // Multiply 8G by 3
        BigInteger[] threeTimes = Secp256k1EC.fractionPoint(point8G, 3, 1);

        // Then halve the result
        BigInteger[] stepwiseFraction = Secp256k1EC.fractionPoint(threeTimes, 1, 2);

        // Assert X coordinates match
        assertTrue(
            directFraction[0].compareTo(stepwiseFraction[0]) == 0,
            "X coordinates should match"
        );

        // Assert Y coordinates match
        assertTrue(
            directFraction[1].compareTo(stepwiseFraction[1]) == 0,
            "Y coordinates should match"
        );
    }

    @Test
    public void testFractionalMultiplyThreeHalvesEqualsThreeThenHalf_Negative8G() {
        // Get 8G
        BigInteger[] point8G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(8, 1));

        // Negate 8G to get -8G
        BigInteger[] negPoint8G = Secp256k1EC.negate(point8G);

        // Multiply -8G by 3/2 directly
        BigInteger[] directFraction = Secp256k1EC.fractionPoint(negPoint8G, 3, 2);

        // Multiply -8G by 3 first
        BigInteger[] threeTimes = Secp256k1EC.fractionPoint(negPoint8G, 3, 1);

        // Then halve the result
        BigInteger[] stepwiseFraction = Secp256k1EC.fractionPoint(threeTimes, 1, 2);

        // Assert X coordinates match
        assertTrue(
            directFraction[0].compareTo(stepwiseFraction[0]) == 0,
            "X coordinates should match"
        );

        // Assert Y coordinates match
        assertTrue(
            directFraction[1].compareTo(stepwiseFraction[1]) == 0,
            "Y coordinates should match"
        );
    }

    @Test
    public void testFractionalMultiplyThreeHalvesEqualsHalfThenThree_Negative8G() {
        // Get 8G
        BigInteger[] point8G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(8, 1));

        // Negate 8G to get -8G
        BigInteger[] negPoint8G = Secp256k1EC.negate(point8G);

        // Multiply -8G by 3/2 directly
        BigInteger[] directFraction = Secp256k1EC.fractionPoint(negPoint8G, 3, 2);

        // Halve -8G first
        BigInteger[] halfFirst = Secp256k1EC.fractionPoint(negPoint8G, 1, 2);

        // Then multiply by 3
        BigInteger[] stepwiseFraction = Secp256k1EC.fractionPoint(halfFirst, 3, 1);

        // Assert X coordinates match
        assertTrue(
            directFraction[0].compareTo(stepwiseFraction[0]) == 0,
            "X coordinates should match"
        );

        // Assert Y coordinates match
        assertTrue(
            directFraction[1].compareTo(stepwiseFraction[1]) == 0,
            "Y coordinates should match"
        );
    }

    @Test
    public void testFractionAddition() {
        BigInteger[] f1 = Secp256k1EC.fraction(2, 3);   // (2/3)G
        BigInteger[] f2 = Secp256k1EC.fraction(1, 4);   // (1/4)G
        BigInteger[] sum = Secp256k1EC.add(f1, f2);     // Should be (11/12)G

        BigInteger[] expected = Secp256k1EC.fraction(11, 12);
        assertEquals(0, sum[0].compareTo(expected[0]));
        assertEquals(0, sum[1].compareTo(expected[1]));
    }

    @Test
    public void testFractionTimesInverseReturnsG() {
        int a = 3;
        int b = 7;

        // (a / b) * G
        BigInteger[] partial = Secp256k1EC.fraction(a, b);

        // Then multiply the result by (b / a)
        BigInteger[] result = Secp256k1EC.fractionPoint(partial, b, a);

        // Compare with G directly
        BigInteger[] expected = Secp256k1EC.G;

        assertEquals(0, result[0].compareTo(expected[0]), "X coordinate should match G");
        assertEquals(0, result[1].compareTo(expected[1]), "Y coordinate should match G");
    }

    @Test
    public void testNegativeFractionTimesNegativeInverseReturnsG() {
        int a = 3;
        int b = 7;

        // (-a / b) * G
        BigInteger[] partial = Secp256k1EC.fraction(-a, b);

        // Multiply by (-b / a)
        BigInteger[] result = Secp256k1EC.fractionPoint(partial, -b, a);

        // Should equal G
        BigInteger[] expected = Secp256k1EC.G;

        assertEquals(0, result[0].compareTo(expected[0]), "X coordinate should match G");
        assertEquals(0, result[1].compareTo(expected[1]), "Y coordinate should match G");
    }

    @Test
    public void testLargeNegativeFractionTimesInverseReturnsG() {
        BigInteger a = new BigInteger("123456789");
        BigInteger b = new BigInteger("987654321");

        // (-a / b) * G
        BigInteger[] partial = Secp256k1EC.sFractionPoint(Secp256k1EC.G, a.negate(), b);

        // Multiply by (-b / a)
        BigInteger[] result = Secp256k1EC.sFractionPoint(partial, b.negate(), a);

        // Should equal G
        BigInteger[] expected = Secp256k1EC.G;

        assertEquals(0, result[0].compareTo(expected[0]), "X coordinate should match G");
        assertEquals(0, result[1].compareTo(expected[1]), "Y coordinate should match G");
    }

    @Test
    public void testFractionAdditionIdentity() {
        int a = 2;
        int b = 3;
        int c = 5;
        int d = 7;

        // Compute (a/b)*G and (c/d)*G
        BigInteger[] frac1 = Secp256k1EC.fraction(a, b); // (2/3)*G
        BigInteger[] frac2 = Secp256k1EC.fraction(c, d); // (5/7)*G

        // Add both fractional points
        BigInteger[] lhs = Secp256k1EC.add(frac1, frac2);

        // Compute expected sum: ((a*d + c*b) / (b*d))*G
        int numerator = a * d + c * b; // 2*7 + 5*3 = 14 + 15 = 29
        int denominator = b * d;       // 3*7 = 21

        BigInteger[] rhs = Secp256k1EC.fraction(numerator, denominator); // (29/21)*G

        // Assert that lhs and rhs match
        assertEquals(0, lhs[0].compareTo(rhs[0]), "X coordinate should match");
        assertEquals(0, lhs[1].compareTo(rhs[1]), "Y coordinate should match");
    }

    @Test
    public void testLargeFractionAdditionIdentity() {
        BigInteger a = new BigInteger("123456789");
        BigInteger b = new BigInteger("987654321");
        BigInteger c = new BigInteger("314159265");
        BigInteger d = new BigInteger("271828183");

        // (a / b) * G and (c / d) * G
        BigInteger[] frac1 = Secp256k1EC.bFraction(a, b);
        BigInteger[] frac2 = Secp256k1EC.bFraction(c, d);

        // Left-hand side: Add both fractional points
        BigInteger[] lhs = Secp256k1EC.add(frac1, frac2);

        // Right-hand side: ((a*d + c*b) / (b*d)) * G
        BigInteger numerator = a.multiply(d).add(c.multiply(b));
        BigInteger denominator = b.multiply(d);

        BigInteger[] rhs = Secp256k1EC.bFraction(numerator, denominator);

        // Assert equality
        assertEquals(0, lhs[0].compareTo(rhs[0]), "X coordinate should match");
        assertEquals(0, lhs[1].compareTo(rhs[1]), "Y coordinate should match");
    }

    @Test
    public void testDoubleHalvingConsistency() {
        BigInteger[] P = Secp256k1EC.G;  // base point G

        // halve once: (1/2)*P
        BigInteger[] halfP = Secp256k1EC.fractionPoint(P, 1, 2);

        // halve again: (1/2)*((1/2)*P) = (1/4)*P
        BigInteger[] doubleHalfP = Secp256k1EC.fractionPoint(halfP, 1, 2);

        // directly quarter: (1/4)*P
        BigInteger[] quarterP = Secp256k1EC.fractionPoint(P, 1, 4);

        assertEquals(0, doubleHalfP[0].compareTo(quarterP[0]), "X coordinates should match");
        assertEquals(0, doubleHalfP[1].compareTo(quarterP[1]), "Y coordinates should match");
    }

    @Test
    public void testNegativeDoubleHalvingConsistency() {
        BigInteger[] P = Secp256k1EC.G;  // base point G

        // Negate P: -P
        BigInteger[] negP = Secp256k1EC.negate(P);

        // halve once: (1/2)*(-P)
        BigInteger[] halfNegP = Secp256k1EC.fractionPoint(negP, 1, 2);

        // halve again: (1/2)*((1/2)*(-P)) = (1/4)*(-P)
        BigInteger[] doubleHalfNegP = Secp256k1EC.fractionPoint(halfNegP, 1, 2);

        // directly quarter: (1/4)*(-P)
        BigInteger[] quarterNegP = Secp256k1EC.fractionPoint(negP, 1, 4);

        assertEquals(0, doubleHalfNegP[0].compareTo(quarterNegP[0]), "X coordinates should match");
        assertEquals(0, doubleHalfNegP[1].compareTo(quarterNegP[1]), "Y coordinates should match");
    }

    @Test
    public void testZeroScalarMultiplication() {
        BigInteger[] zeroPoint = Secp256k1EC.multiply(Secp256k1EC.G,
        		Secp256k1EC.fraction(0,1));

        // Point at infinity is represented as (0,0) in your implementation
        BigInteger[] expected = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };

        assertEquals(0, zeroPoint[0].compareTo(expected[0]), "X coordinate should be 0 for point at infinity");
        assertEquals(0, zeroPoint[1].compareTo(expected[1]), "Y coordinate should be 0 for point at infinity");
    }

    @Test
    public void testAdditiveIdentity() {
        // Get any point, e.g., 7G
        BigInteger[] point7G = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(7, 1));

        // Get the zero point (point at infinity)
        BigInteger[] zeroPoint = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(0, 1));

        // Add zero point to 7G
        BigInteger[] sum = Secp256k1EC.add(point7G, zeroPoint);

        // Assert sum equals the original point
        assertEquals(point7G[0], sum[0], "X coordinate should remain unchanged when adding zero point");
        assertEquals(point7G[1], sum[1], "Y coordinate should remain unchanged when adding zero point");
    }

    @Test
    public void testAdditiveInverse() {
        // Take some point, e.g., 7G
        BigInteger[] point = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(7, 1));
        // Negate the point
        BigInteger[] negPoint = Secp256k1EC.negate(point);

        // Add the point and its negation
        BigInteger[] sum = Secp256k1EC.add(point, negPoint);

        // Zero point expected
        BigInteger[] zeroPoint = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(0, 1));

        assertEquals(zeroPoint[0], sum[0], "X should be zero for additive inverse sum");
        assertEquals(zeroPoint[1], sum[1], "Y should be zero for additive inverse sum");
    }

    @Test
    public void testScalarOneMultiplication() {
        BigInteger[] result = Secp256k1EC.multiply(Secp256k1EC.G, Secp256k1EC.fraction(1,1));

        // The result should equal G itself
        assertEquals(0, result[0].compareTo(Secp256k1EC.G[0]), "X coordinate should be equal to G's X");
        assertEquals(0, result[1].compareTo(Secp256k1EC.G[1]), "Y coordinate should be equal to G's Y");
    }

    @Test
    public void testLargeFractionAdditionIdentityFour() {
        BigInteger a = new BigInteger("98765432123456789");
        BigInteger b = new BigInteger("123456789987654321");
        BigInteger c = new BigInteger("112233445566778899");
        BigInteger d = new BigInteger("998877665544332211");

        // (a / b) * G and (c / d) * G
        BigInteger[] frac1 = Secp256k1EC.bFraction(a, b);
        BigInteger[] frac2 = Secp256k1EC.bFraction(c, d);

        // Left-hand side: Add the two fractional points
        BigInteger[] lhs = Secp256k1EC.add(frac1, frac2);

        // Right-hand side: ((a*d + c*b) / (b*d)) * G
        BigInteger numerator = a.multiply(d).add(c.multiply(b));
        BigInteger denominator = b.multiply(d);

        BigInteger[] rhs = Secp256k1EC.bFraction(numerator, denominator);

        // Assert equality
        assertEquals(0, lhs[0].compareTo(rhs[0]), "X coordinate should match");
        assertEquals(0, lhs[1].compareTo(rhs[1]), "Y coordinate should match");
    }

    @Test
    public void testLargeFractionAdditionIdentityNegativeFour() {
        BigInteger a = new BigInteger("98765432123456789");
        BigInteger b = new BigInteger("123456789987654321");
        BigInteger c = new BigInteger("112233445566778899");
        BigInteger d = new BigInteger("998877665544332211");

        // (-a / b) * G and (-c / d) * G
        BigInteger[] frac1 = Secp256k1EC.bFraction(a.negate(), b);
        BigInteger[] frac2 = Secp256k1EC.bFraction(c.negate(), d);

        // Left-hand side: Add the two fractional points
        BigInteger[] lhs = Secp256k1EC.add(frac1, frac2);

        // Right-hand side: ((-a*d + -c*b) / (b*d)) * G
        BigInteger numerator = a.negate().multiply(d).add(c.negate().multiply(b));
        BigInteger denominator = b.multiply(d);

        BigInteger[] rhs = Secp256k1EC.bFraction(numerator, denominator);

        // Assert equality
        assertEquals(0, lhs[0].compareTo(rhs[0]), "X coordinate should match");
        assertEquals(0, lhs[1].compareTo(rhs[1]), "Y coordinate should match");
    }

    @Test
    public void testNegationOfFractionEqualsFractionOfNegatedPoint() {
        // Use some fraction a/b
        int a = 7;
        int b = 13;

        // Get base point G
        BigInteger[] G = Secp256k1EC.G;

        // fraction * (-P)
        BigInteger[] negP = Secp256k1EC.negate(G);                    // -P
        BigInteger[] left = Secp256k1EC.fractionPoint(negP, a, b);    // (a/b)*(-P)

        // - (fraction * P)
        BigInteger[] fracP = Secp256k1EC.fractionPoint(G, a, b);      // (a/b)*P
        BigInteger[] right = Secp256k1EC.negate(fracP);               // -((a/b)*P)

        // Assert both sides are equal
        assertEquals(0, left[0].compareTo(right[0]), "X coordinate should match");
        assertEquals(0, left[1].compareTo(right[1]), "Y coordinate should match");
    }


    // -------------------
    // Utility
    // -------------------

    private BigInteger randScalar() {
        while (true) {
            BigInteger r = new BigInteger(N.bitLength(), random).mod(N);
            if (!r.equals(BigInteger.ZERO)) return r;
        }
    }
}
