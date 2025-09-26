package com.github.chainfs.ecc9;
import java.math.BigInteger;

public class NistP256Curve {

    // Prime modulus p = 2^256 - 2^224 + 2^192 + 2^96 - 1
    public static final BigInteger P = new BigInteger(
            "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16);

    // Curve coefficients: y^2 = x^3 + ax + b
    public static final BigInteger A = new BigInteger("-3");
    public static final BigInteger B = new BigInteger(
            "5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);

    // Base point G (generator)
    public static final BigInteger Gx = new BigInteger(
            "6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16);
    public static final BigInteger Gy = new BigInteger(
            "4fe342e2fe1a7f9b8ee7eb4a7c0f9e162cbce33576b315ececbb6406837bf51f", 16);

    // Order of the base point
    public static final BigInteger N = new BigInteger(
            "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16);

    // Identity point (point at infinity) represented as null
    public static final ECPoint INFINITY = null;

    public static class ECPoint {
        public final BigInteger x;
        public final BigInteger y;

        public ECPoint(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ECPoint)) return false;
            ECPoint other = (ECPoint) o;
            return x.equals(other.x) && y.equals(other.y);
        }

        @Override
        public int hashCode() {
            return x.hashCode() ^ y.hashCode();
        }

        @Override
        public String toString() {
            return "(" + x.toString(16) + ", " + y.toString(16) + ")";
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

    // Modular inversion
    public static BigInteger modInv(BigInteger a) {
        return a.modInverse(P);
    }

    // Modular exponentiation
    public static BigInteger modPow(BigInteger base, BigInteger exp) {
        return base.modPow(exp, P);
    }

    // Point addition on the curve
    public static ECPoint pointAdd(ECPoint p1, ECPoint p2) {
        if (p1 == INFINITY) return p2;
        if (p2 == INFINITY) return p1;

        if (p1.x.equals(p2.x)) {
            if (p1.y.equals(p2.y)) {
                return pointDouble(p1);
            } else {
                // p1.x == p2.x but y1 != y2, result is infinity
                return INFINITY;
            }
        }

        BigInteger lambda = modMul(
                modSub(p2.y, p1.y),
                modInv(modSub(p2.x, p1.x))
        );

        BigInteger x3 = modSub(modSub(modPow(lambda, BigInteger.TWO), p1.x), p2.x);
        BigInteger y3 = modSub(modMul(lambda, modSub(p1.x, x3)), p1.y);

        return new ECPoint(x3, y3);
    }

    // Point doubling on the curve
    public static ECPoint pointDouble(ECPoint p) {
        if (p == INFINITY) return INFINITY;

        BigInteger threeX2 = modMul(BigInteger.valueOf(3), modPow(p.x, BigInteger.TWO));
        BigInteger lambda = modMul(
                modAdd(threeX2, A),
                modInv(modMul(BigInteger.valueOf(2), p.y))
        );

        BigInteger x3 = modSub(modPow(lambda, BigInteger.TWO), modMul(BigInteger.valueOf(2), p.x));
        BigInteger y3 = modSub(modMul(lambda, modSub(p.x, x3)), p.y);

        return new ECPoint(x3, y3);
    }

    // Scalar multiplication using double-and-add
    public static ECPoint scalarMultiply(BigInteger k, ECPoint point) {
        ECPoint result = INFINITY;
        ECPoint addend = point;

        int length = k.bitLength();
        for (int i = length - 1; i >= 0; i--) {
            result = pointDouble(result);
            if (k.testBit(i)) {
                result = pointAdd(result, addend);
            }
        }
        return result;
    }

    // Check if point is on the curve
    public static boolean isOnCurve(ECPoint p) {
        if (p == INFINITY) return true;

        // y^2 mod p
        BigInteger lhs = modPow(p.y, BigInteger.TWO);

        // x^3 + ax + b mod p
        BigInteger rhs = modAdd(
                modAdd(modPow(p.x, BigInteger.valueOf(3)), modMul(A, p.x)),
                B
        );

        return lhs.equals(rhs);
    }

    public static void main(String[] args) {
        ECPoint G = new ECPoint(Gx, Gy);
        System.out.println("Generator G: " + G);
        System.out.println("G on curve? " + isOnCurve(G));

        // Scalar multiply 1 (should be G)
        ECPoint oneG = scalarMultiply(BigInteger.ONE, G);
        System.out.println("1 * G = " + oneG);

        // Scalar multiply 0 (should be infinity)
        ECPoint zeroG = scalarMultiply(BigInteger.ZERO, G);
        System.out.println("0 * G = " + zeroG);

        // Scalar multiply 2 (should be doubling G)
        ECPoint twoG = scalarMultiply(BigInteger.valueOf(2), G);
        ECPoint doubledG = pointDouble(G);
        System.out.println("2 * G = " + twoG);
        System.out.println("Double G = " + doubledG);
        System.out.println("2G == doubleG? " + twoG.equals(doubledG));
    }
}
