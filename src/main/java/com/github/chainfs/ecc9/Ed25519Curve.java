package com.github.chainfs.ecc9;
import java.math.BigInteger;
import java.util.Arrays;

public class Ed25519Curve {
    // Prime p = 2^255 - 19
    public static final BigInteger P = BigInteger.valueOf(2).pow(255).subtract(BigInteger.valueOf(19));

    // Curve parameter d = -121665/121666 mod p
    // Using the constant value for d from RFC8032:
    // d = -121665 * inv(121666) mod p
    public static final BigInteger D = new BigInteger("37095705934669439343138083508754565189542113879843219016388785533085940283555");

    // Base point coordinates (x, y) on the curve (from RFC8032)
    public static final BigInteger BASE_X = new BigInteger("15112221349535400772501151409588531511454012693041857206046113283949847762202");
    public static final BigInteger BASE_Y = new BigInteger("46316835694926478169428394003475163141307993866256225615783033603165251855960");

    // Identity point (neutral element)
    public static final EdPoint IDENTITY = new EdPoint(BigInteger.ZERO, BigInteger.ONE);

    public static class EdPoint {
        public final BigInteger x;
        public final BigInteger y;

        public EdPoint(BigInteger x, BigInteger y) {
            this.x = x.mod(P);
            this.y = y.mod(P);
        }

        @Override
        public String toString() {
            return "EdPoint{x=" + x + ", y=" + y + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EdPoint)) return false;
            EdPoint other = (EdPoint) o;
            return x.equals(other.x) && y.equals(other.y);
        }
    }

    // Modular addition
    public static BigInteger modAdd(BigInteger a, BigInteger b) {
        return a.add(b).mod(P);
    }

    // Modular subtraction
    public static BigInteger modSub(BigInteger a, BigInteger b) {
        return a.subtract(b).mod(P);
    }

    // Modular multiplication
    public static BigInteger modMul(BigInteger a, BigInteger b) {
        return a.multiply(b).mod(P);
    }

    // Modular inverse
    public static BigInteger modInv(BigInteger a) {
        return a.modInverse(P);
    }

    // Point addition on twisted Edwards curve
    // Formula:
    // x3 = (x1*y2 + y1*x2) / (1 + d*x1*x2*y1*y2)
    // y3 = (y1*y2 - x1*x2) / (1 - d*x1*x2*y1*y2)
    public static EdPoint pointAdd(EdPoint P1, EdPoint P2) {
        BigInteger x1 = P1.x, y1 = P1.y;
        BigInteger x2 = P2.x, y2 = P2.y;

        BigInteger x1x2 = modMul(x1, x2);
        BigInteger y1y2 = modMul(y1, y2);
        BigInteger dxy = modMul(D, modMul(x1x2, y1y2));

        BigInteger numeratorX = modAdd(modMul(x1, y2), modMul(y1, x2));
        BigInteger denominatorX = modAdd(BigInteger.ONE, dxy);

        BigInteger numeratorY = modSub(y1y2, x1x2);
        BigInteger denominatorY = modSub(BigInteger.ONE, dxy);

        BigInteger x3 = modMul(numeratorX, modInv(denominatorX));
        BigInteger y3 = modMul(numeratorY, modInv(denominatorY));

        return new EdPoint(x3, y3);
    }

    // Point doubling is pointAdd(P, P)
    public static EdPoint pointDouble(EdPoint P) {
        return pointAdd(P, P);
    }

    // Scalar multiplication (double and add)
    public static EdPoint scalarMultiply(BigInteger k, EdPoint P) {
        EdPoint result = IDENTITY;
        EdPoint addend = P;

        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = pointDouble(result);
            if (k.testBit(i)) {
                result = pointAdd(result, addend);
            }
        }
        return result;
    }

    // Example usage
    public static void main(String[] args) {
        EdPoint base = new EdPoint(BASE_X, BASE_Y);
        BigInteger scalar = new BigInteger("123456789012345678901234567890");

        EdPoint result = scalarMultiply(scalar, base);

        System.out.println("Resulting point:");
        System.out.println("X = " + result.x);
        System.out.println("Y = " + result.y);
    }
}
