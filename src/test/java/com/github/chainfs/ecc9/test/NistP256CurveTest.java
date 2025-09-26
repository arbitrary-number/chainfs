package com.github.chainfs.ecc9.test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.NistP256Curve;

public class NistP256CurveTest {

    @Test
    public void testGeneratorOnCurve() {
        NistP256Curve.ECPoint G = new NistP256Curve.ECPoint(NistP256Curve.Gx, NistP256Curve.Gy);
        assertTrue(NistP256Curve.isOnCurve(G), "Generator point G should be on the curve");
    }

    @Test
    public void testPointAtInfinity() {
        NistP256Curve.ECPoint infinity = NistP256Curve.INFINITY;
        assertNull(infinity, "Infinity point should be null");
        assertTrue(NistP256Curve.isOnCurve(infinity), "Infinity point should be on curve");
    }

    @Test
    public void testPointDoubling() {
        NistP256Curve.ECPoint G = new NistP256Curve.ECPoint(NistP256Curve.Gx, NistP256Curve.Gy);
        NistP256Curve.ECPoint doubleG = NistP256Curve.pointDouble(G);
        NistP256Curve.ECPoint scalar2G = NistP256Curve.scalarMultiply(BigInteger.valueOf(2), G);
        assertEquals(doubleG, scalar2G, "Point doubling should equal scalar multiplication by 2");
        assertTrue(NistP256Curve.isOnCurve(doubleG), "Doubled point should be on the curve");
    }

    @Test
    public void testScalarMultiplyZero() {
        NistP256Curve.ECPoint G = new NistP256Curve.ECPoint(NistP256Curve.Gx, NistP256Curve.Gy);
        NistP256Curve.ECPoint zeroG = NistP256Curve.scalarMultiply(BigInteger.ZERO, G);
        assertNull(zeroG, "Scalar multiplication by 0 should yield point at infinity (null)");
    }

    @Test
    public void testScalarMultiplyOne() {
        NistP256Curve.ECPoint G = new NistP256Curve.ECPoint(NistP256Curve.Gx, NistP256Curve.Gy);
        NistP256Curve.ECPoint oneG = NistP256Curve.scalarMultiply(BigInteger.ONE, G);
        assertEquals(G, oneG, "Scalar multiplication by 1 should yield the same point");
    }

    @Test
    public void testPointAddition() {
        NistP256Curve.ECPoint G = new NistP256Curve.ECPoint(NistP256Curve.Gx, NistP256Curve.Gy);
        NistP256Curve.ECPoint infinity = NistP256Curve.INFINITY;

        // G + infinity = G
        assertEquals(G, NistP256Curve.pointAdd(G, infinity), "G + infinity should be G");
        assertEquals(G, NistP256Curve.pointAdd(infinity, G), "infinity + G should be G");

        // G + G = 2G (point doubling)
        NistP256Curve.ECPoint addGG = NistP256Curve.pointAdd(G, G);
        NistP256Curve.ECPoint doubleG = NistP256Curve.pointDouble(G);
        assertEquals(doubleG, addGG, "G + G should equal point doubling");

        // 2G + G = 3G
        NistP256Curve.ECPoint threeG = NistP256Curve.scalarMultiply(BigInteger.valueOf(3), G);
        NistP256Curve.ECPoint add2GG = NistP256Curve.pointAdd(doubleG, G);
        assertEquals(threeG, add2GG, "2G + G should equal 3G");
    }
}
