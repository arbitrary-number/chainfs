package com.github.chainfs.ecc2;

import java.math.BigInteger;
import java.util.Objects;

public class Secp256k1 {

    public static final BigInteger p = new BigInteger(
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
    public static final BigInteger a = BigInteger.ZERO;
    public static final BigInteger b = BigInteger.valueOf(7);

    // Point at infinity: represented as {null, null}
    public static final BigInteger[] INF = new BigInteger[] { null, null };

    public boolean isInfinity(BigInteger[] P) {
        return P[0] == null || P[1] == null;
    }

    public boolean isOnCurve(BigInteger[] P) {
        if (isInfinity(P)) return true;
        BigInteger x = P[0], y = P[1];
        return y.modPow(BigInteger.TWO, p)
                .equals(x.modPow(BigInteger.valueOf(3), p).add(b).mod(p));
    }

    public BigInteger[] negate(BigInteger[] P) {
        if (isInfinity(P)) return INF;
        return new BigInteger[] { P[0], P[1].negate().mod(p) };
    }

    public BigInteger[] add(BigInteger[] P, BigInteger[] Q) {
        if (isInfinity(P)) return Q;
        if (isInfinity(Q)) return P;
        BigInteger x1 = P[0], y1 = P[1];
        BigInteger x2 = Q[0], y2 = Q[1];

        if (x1.equals(x2)) {
            if (y1.equals(y2)) {
                return doublePoint(P);
            } else {
                return INF; // P + (-P) = 0
            }
        }

        BigInteger lambda = y2.subtract(y1)
                              .multiply(x2.subtract(x1).modInverse(p))
                              .mod(p);
        BigInteger x3 = lambda.pow(2).subtract(x1).subtract(x2).mod(p);
        BigInteger y3 = lambda.multiply(x1.subtract(x3)).subtract(y1).mod(p);
        return new BigInteger[] { x3, y3 };
    }

    public BigInteger[] doublePoint(BigInteger[] P) {
        if (isInfinity(P)) return INF;

        BigInteger x = P[0], y = P[1];
        if (y.equals(BigInteger.ZERO)) return INF;

        BigInteger numerator = BigInteger.valueOf(3).multiply(x.pow(2));
        BigInteger denominator = BigInteger.TWO.multiply(y).modInverse(p);
        BigInteger lambda = numerator.multiply(denominator).mod(p);
        BigInteger x3 = lambda.pow(2).subtract(x.multiply(BigInteger.TWO)).mod(p);
        BigInteger y3 = lambda.multiply(x.subtract(x3)).subtract(y).mod(p);
        return new BigInteger[] { x3, y3 };
    }

    public BigInteger[] multiply(BigInteger[] P, BigInteger k) {
        BigInteger[] R = INF;
        BigInteger[] Q = P;

        for (int i = k.bitLength() - 1; i >= 0; i--) {
            R = doublePoint(R);
            if (k.testBit(i)) {
                R = add(R, Q);
            }
        }
        return R;
    }
//
//    // Division polynomial ψ_l(x) for small l (odd primes)
//    public BigInteger divisionPolynomial(BigInteger x, int l) {
//        if (l < 2) throw new IllegalArgumentException("l must be >= 2");
//        if (l == 2) return BigInteger.TWO;
//        if (l == 3) return x.pow(4).add(BigInteger.valueOf(7 * 3).multiply(x));
//        if (l == 5) {
//            BigInteger x2 = x.pow(2);
//            BigInteger x3 = x.pow(3);
//            BigInteger x4 = x.pow(4);
//            BigInteger x5 = x.pow(5);
//            BigInteger x6 = x.pow(6);
//            return x12(7).mod(p); // Fill this in properly
//        }
//        throw new UnsupportedOperationException("Only ψ3(x) supported for now");
//    }

    public BigInteger divisionPolynomial(BigInteger x, int l) {
        if (l < 2) throw new IllegalArgumentException("l must be >= 2");

        x = x.mod(p); // Always reduce x mod p

        if (l == 2) return BigInteger.TWO;
        if (l == 3) {
            return BigInteger.valueOf(3).multiply(x.pow(4)).mod(p);
        }
        //ψ5​(x)=5x12+700x9+12250x6+61250x3+42875
        if (l == 5) {
            // ψ₅(x) = 5x¹² + 700x⁹ + 12250x⁶ + 61250x³ + 42875
            BigInteger x3 = x.pow(3).mod(p);
            BigInteger x6 = x3.pow(2).mod(p);
            BigInteger x9 = x6.multiply(x3).mod(p);
            BigInteger x12 = x6.pow(2).mod(p);

            BigInteger term1 = BigInteger.valueOf(5).multiply(x12).mod(p);
            BigInteger term2 = BigInteger.valueOf(700).multiply(x9).mod(p);
            BigInteger term3 = BigInteger.valueOf(12250).multiply(x6).mod(p);
            BigInteger term4 = BigInteger.valueOf(61250).multiply(x3).mod(p);
            BigInteger term5 = BigInteger.valueOf(42875).mod(p);

            return term1
                .add(term2)
                .add(term3)
                .add(term4)
                .add(term5)
                .mod(p);
        } else if (l == 7) {
            BigInteger x3 = x.pow(3).mod(p);
            BigInteger x6 = x3.pow(2).mod(p);
            BigInteger x9 = x6.multiply(x3).mod(p);
            BigInteger x12 = x6.pow(2).mod(p);
            BigInteger x15 = x12.multiply(x3).mod(p);
            BigInteger x18 = x12.multiply(x6).mod(p);
            BigInteger x21 = x18.multiply(x3).mod(p);
            BigInteger x24 = x12.pow(2).mod(p);

            BigInteger term1 = BigInteger.valueOf(7).multiply(x24).mod(p);
            BigInteger term2 = BigInteger.valueOf(9800).multiply(x21).mod(p);
            BigInteger term3 = BigInteger.valueOf(403020).multiply(x18).mod(p);
            BigInteger term4 = BigInteger.valueOf(6157400).multiply(x15).mod(p);
            BigInteger term5 = BigInteger.valueOf(40458240).multiply(x12).mod(p);
            BigInteger term6 = BigInteger.valueOf(115259600).multiply(x9).mod(p);
            BigInteger term7 = BigInteger.valueOf(145860000).multiply(x6).mod(p);
            BigInteger term8 = BigInteger.valueOf(71890500).multiply(x3).mod(p);
            BigInteger term9 = BigInteger.valueOf(10084275).mod(p);

            return term1
                .add(term2)
                .add(term3)
                .add(term4)
                .add(term5)
                .add(term6)
                .add(term7)
                .add(term8)
                .add(term9)
                .mod(p);
        }

        throw new UnsupportedOperationException("Only ψ3(x), ψ5(x) and ψ7(x) supported for now");
    }

//    public BigInteger divisionPolynomial(BigInteger x, int l) {
//        x = x.mod(p);
//
//        if (l < 2) throw new IllegalArgumentException("l must be >= 2");
//        if (l == 2) return BigInteger.TWO;
//        if (l == 3) {
//            return BigInteger.valueOf(3).multiply(x.pow(4)).mod(p);
//        }
//        if (l == 5) {
//            BigInteger x3 = x.pow(3).mod(p);
//            BigInteger x6 = x3.pow(2).mod(p);
//            BigInteger x9 = x6.multiply(x3).mod(p);
//            BigInteger x12 = x6.pow(2).mod(p);
//
//            BigInteger term1 = BigInteger.valueOf(5).multiply(x12).mod(p);
//            BigInteger term2 = BigInteger.valueOf(700).multiply(x9).mod(p);
//            BigInteger term3 = BigInteger.valueOf(12250).multiply(x6).mod(p);
//            BigInteger term4 = BigInteger.valueOf(61250).multiply(x3).mod(p);
//            BigInteger term5 = BigInteger.valueOf(42875).mod(p);
//
//            return term1.add(term2).add(term3).add(term4).add(term5).mod(p);
//        }
//        if (l == 7) {
//            BigInteger x3 = x.pow(3).mod(p);
//            BigInteger x6 = x3.pow(2).mod(p);
//            BigInteger x9 = x6.multiply(x3).mod(p);
//            BigInteger x12 = x6.pow(2).mod(p);
//            BigInteger x15 = x12.multiply(x3).mod(p);
//            BigInteger x18 = x12.multiply(x6).mod(p);
//            BigInteger x21 = x18.multiply(x3).mod(p);
//            BigInteger x24 = x12.pow(2).mod(p);
//
//            BigInteger term1 = BigInteger.valueOf(7).multiply(x24).mod(p);
//            BigInteger term2 = BigInteger.valueOf(9800).multiply(x21).mod(p);
//            BigInteger term3 = BigInteger.valueOf(403020).multiply(x18).mod(p);
//            BigInteger term4 = BigInteger.valueOf(6157400).multiply(x15).mod(p);
//            BigInteger term5 = BigInteger.valueOf(40458240).multiply(x12).mod(p);
//            BigInteger term6 = BigInteger.valueOf(115259600).multiply(x9).mod(p);
//            BigInteger term7 = BigInteger.valueOf(145860000).multiply(x6).mod(p);
//            BigInteger term8 = BigInteger.valueOf(71890500).multiply(x3).mod(p);
//            BigInteger term9 = BigInteger.valueOf(10084275).mod(p);
//
//            return term1
//                .add(term2)
//                .add(term3)
//                .add(term4)
//                .add(term5)
//                .add(term6)
//                .add(term7)
//                .add(term8)
//                .add(term9)
//                .mod(p);
//        }
//
//        throw new UnsupportedOperationException("Only ψ3(x), ψ5(x), and ψ7(x) supported for now");
//    }



    // Sample ψ3(x): ψ_3(x) = 3x^4 + 21a x^2 + 28b x - a^2 (but a = 0 here)
    public BigInteger divisionPolynomial3(BigInteger x) {
        return BigInteger.valueOf(3).multiply(x.pow(4)).mod(p);
    }

    // Convenience: print point
    public static String pointToString(BigInteger[] P) {
        if (P == null || P[0] == null) return "INF";
        return "(" + P[0].toString(16) + ", " + P[1].toString(16) + ")";
    }

}
