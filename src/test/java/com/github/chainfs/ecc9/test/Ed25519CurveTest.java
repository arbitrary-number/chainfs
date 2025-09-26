package com.github.chainfs.ecc9.test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.Ed25519Curve;

public class Ed25519CurveTest {

    @Test
    public void testModAdd() {
        BigInteger a = new BigInteger("123456789");
        BigInteger b = new BigInteger("987654321");
        BigInteger expected = a.add(b).mod(Ed25519Curve.P);
        assertEquals(expected, Ed25519Curve.modAdd(a, b));
    }

    @Test
    public void testModSub() {
        BigInteger a = new BigInteger("1000");
        BigInteger b = new BigInteger("500");
        BigInteger expected = a.subtract(b).mod(Ed25519Curve.P);
        assertEquals(expected, Ed25519Curve.modSub(a, b));
    }

    @Test
    public void testModMul() {
        BigInteger a = new BigInteger("12345");
        BigInteger b = new BigInteger("67890");
        BigInteger expected = a.multiply(b).mod(Ed25519Curve.P);
        assertEquals(expected, Ed25519Curve.modMul(a, b));
    }

    @Test
    public void testModInv() {
        BigInteger a = new BigInteger("123456789");
        BigInteger inv = Ed25519Curve.modInv(a);
        assertEquals(BigInteger.ONE, Ed25519Curve.modMul(a, inv));
    }

    @Test
    public void testPointAdditionIdentity() {
        Ed25519Curve.EdPoint base = new Ed25519Curve.EdPoint(Ed25519Curve.BASE_X, Ed25519Curve.BASE_Y);
        Ed25519Curve.EdPoint sum = Ed25519Curve.pointAdd(base, Ed25519Curve.IDENTITY);
        assertEquals(base, sum);
    }

    @Test
    public void testPointDoubling() {
        Ed25519Curve.EdPoint base = new Ed25519Curve.EdPoint(Ed25519Curve.BASE_X, Ed25519Curve.BASE_Y);
        Ed25519Curve.EdPoint doubled = Ed25519Curve.pointDouble(base);
        // Doubling should be same as base + base
        Ed25519Curve.EdPoint added = Ed25519Curve.pointAdd(base, base);
        assertEquals(added, doubled);
    }

    @Test
    public void testScalarMultiplicationByZero() {
        Ed25519Curve.EdPoint base = new Ed25519Curve.EdPoint(Ed25519Curve.BASE_X, Ed25519Curve.BASE_Y);
        Ed25519Curve.EdPoint zeroMul = Ed25519Curve.scalarMultiply(BigInteger.ZERO, base);
        assertEquals(Ed25519Curve.IDENTITY, zeroMul);
    }

    @Test
    public void testScalarMultiplicationByOne() {
        Ed25519Curve.EdPoint base = new Ed25519Curve.EdPoint(Ed25519Curve.BASE_X, Ed25519Curve.BASE_Y);
        Ed25519Curve.EdPoint oneMul = Ed25519Curve.scalarMultiply(BigInteger.ONE, base);
        assertEquals(base, oneMul);
    }

    @Test
    public void testScalarMultiplicationBasic() {
        Ed25519Curve.EdPoint base = new Ed25519Curve.EdPoint(Ed25519Curve.BASE_X, Ed25519Curve.BASE_Y);
        BigInteger scalar = new BigInteger("2");
        Ed25519Curve.EdPoint result = Ed25519Curve.scalarMultiply(scalar, base);
        Ed25519Curve.EdPoint doubled = Ed25519Curve.pointDouble(base);
        assertEquals(doubled, result);
    }
}
