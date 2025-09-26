package com.github.chainfs.ecc9.test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.Secp256k1EC;

public class Secp256k1ECTest {

    private static final BigInteger[] G = new BigInteger[]{Secp256k1EC.GX, Secp256k1EC.GY};

    @Test
    public void testHalfThenDoubleEqualsOriginalPoint() {
        // Halve G
        BigInteger[] halfG = Secp256k1EC.scalarDivide(BigInteger.TWO, G[0], G[1]);

        // Double the result
        BigInteger[] halfGJac = new BigInteger[]{halfG[0], halfG[1], BigInteger.ONE};
        BigInteger[] doubledJac = Secp256k1EC.pointDouble(halfGJac[0], halfGJac[1], halfGJac[2]);
        BigInteger[] doubledAffine = Secp256k1EC.toAffinePoint(doubledJac);

        // Assert x and y are equal
        assertEquals(G[0], doubledAffine[0], "X coordinate mismatch after halving and doubling");
        assertEquals(G[1], doubledAffine[1], "Y coordinate mismatch after halving and doubling");
    }

    @Test
    public void testHalfGDistanceBetween8GAnd9G() {
        // L = 8G / 2 = 4G
        BigInteger[] point8G = Secp256k1EC.scalarMultiply(BigInteger.valueOf(8), G[0], G[1]);
        BigInteger[] L = Secp256k1EC.scalarDivide(BigInteger.TWO, point8G[0], point8G[1]);

        // R = 9G / 2 = 4.5G
        BigInteger[] point9G = Secp256k1EC.scalarMultiply(BigInteger.valueOf(9), G[0], G[1]);
        BigInteger[] R = Secp256k1EC.scalarDivide(BigInteger.TWO, point9G[0], point9G[1]);

        // Expected difference: 0.5G
        BigInteger[] halfG = Secp256k1EC.scalarDivide(BigInteger.TWO, G[0], G[1]);

        // Compute diff = R - L
        BigInteger[] diff = Secp256k1EC.subtract(R, L);

        // Assert the difference is 0.5G
        assertEquals(halfG[0], diff[0], "X coordinate of difference is not 0.5G");
        assertEquals(halfG[1], diff[1], "Y coordinate of difference is not 0.5G");
    }

    @Test
    public void testHalfGDistanceBetween20GAnd21GWithRedoubling() {
        // Step 1: Compute 20G and 21G
        BigInteger[] point20G = Secp256k1EC.scalarMultiply(BigInteger.valueOf(20), Secp256k1EC.GX, Secp256k1EC.GY);
        BigInteger[] point21G = Secp256k1EC.scalarMultiply(BigInteger.valueOf(21), Secp256k1EC.GX, Secp256k1EC.GY);

        // Step 2: Divide both by 2
        BigInteger[] L = Secp256k1EC.scalarDivide(BigInteger.TWO, point20G[0], point20G[1]); // 10G
        BigInteger[] R = Secp256k1EC.scalarDivide(BigInteger.TWO, point21G[0], point21G[1]); // 10.5G

        // Step 3: Check that R - L = 0.5G
        BigInteger[] halfG = Secp256k1EC.scalarDivide(BigInteger.TWO, Secp256k1EC.GX, Secp256k1EC.GY);
        BigInteger[] diff = Secp256k1EC.subtract(R, L);

        assertEquals(halfG[0], diff[0], "X coordinate of R - L is not 0.5G");
        assertEquals(halfG[1], diff[1], "Y coordinate of R - L is not 0.5G");

        // Step 4: Redouble L and R, compare with original
        BigInteger[] doubledLJac = Secp256k1EC.pointDouble(L[0], L[1], BigInteger.ONE);
        BigInteger[] doubledLAffine = Secp256k1EC.toAffinePoint(doubledLJac);

        BigInteger[] doubledRJac = Secp256k1EC.pointDouble(R[0], R[1], BigInteger.ONE);
        BigInteger[] doubledRAffine = Secp256k1EC.toAffinePoint(doubledRJac);

        // Assert 2L == 20G
        assertEquals(point20G[0], doubledLAffine[0], "X coordinate of 2L does not match 20G");
        assertEquals(point20G[1], doubledLAffine[1], "Y coordinate of 2L does not match 20G");

        // Assert 2R == 21G
        assertEquals(point21G[0], doubledRAffine[0], "X coordinate of 2R does not match 21G");
        assertEquals(point21G[1], doubledRAffine[1], "Y coordinate of 2R does not match 21G");
    }

    @Test
    public void testFractionAdditionOneThirdPlusOneSixthEqualsOneHalf() {
        // Compute 1/3 G
        BigInteger[] oneThirdG = Secp256k1EC.fraction(1, 3);  // returns [x, y, 1, 3]
        // Compute 1/6 G
        BigInteger[] oneSixthG = Secp256k1EC.fraction(1, 6);  // returns [x, y, 1, 6]

        // Add the two: (1/3 + 1/6)G
        BigInteger[] sum = Secp256k1EC.add(oneThirdG, oneSixthG);

        // Compute 1/2 G
        BigInteger[] halfG = Secp256k1EC.fraction(1, 2);

        // Assert sum == 1/2 G
        assertEquals(halfG[0], sum[0], "X coordinate of (1/3 + 1/6)G does not match 1/2 G");
        assertEquals(halfG[1], sum[1], "Y coordinate of (1/3 + 1/6)G does not match 1/2 G");
    }
}
