package com.github.chainfs.ecc9.test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.chainfs.ecc9.Secp256k1EC;

import java.math.BigInteger;
import java.util.stream.Stream;

public class FractionArithmeticTest {

    // Structure: [numerator1, denom1, numerator2, denom2, expected numerator, expected denom]
    static Stream<int[]> fractionAddTestVectors() {
        return Stream.of(
            new int[]{1, 4, 1, 4, 1, 2},    // 1/4 + 1/4 = 1/2
            new int[]{2, 3, -1, 3, 1, 3},   // 2/3 - 1/3 = 1/3
            new int[]{3, 5, 2, 5, 1, 1},    // 3/5 + 2/5 = 1
            new int[]{1, 2, 1, 2, 1, 1},    // 1/2 + 1/2 = 1
            new int[]{3, 7, 2, 7, 5, 7}     // 3/7 + 2/7 = 5/7
        );
    }

    @ParameterizedTest
    @MethodSource("fractionAddTestVectors")
    public void testFractionAddition(int[] vector) {
        int n1 = vector[0], d1 = vector[1];
        int n2 = vector[2], d2 = vector[3];
        int expectedNum = vector[4], expectedDen = vector[5];

        // Compute first and second fractions of G
        BigInteger[] f1 = Secp256k1EC.fraction(n1, d1);  // f1 = (n1/d1) * G
        BigInteger[] f2 = Secp256k1EC.fraction(n2, d2);  // f2 = (n2/d2) * G

        // Add them
        BigInteger[] sum = Secp256k1EC.add(f1, f2);

        // Expected fraction result
        BigInteger[] expected = Secp256k1EC.fraction(expectedNum, expectedDen);

        assertEquals(expected[0], sum[0], "X mismatch for (" + n1 + "/" + d1 + ") + (" + n2 + "/" + d2 + ")");
        assertEquals(expected[1], sum[1], "Y mismatch for (" + n1 + "/" + d1 + ") + (" + n2 + "/" + d2 + ")");
    }
}
