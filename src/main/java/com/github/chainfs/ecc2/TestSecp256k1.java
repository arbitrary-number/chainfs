package com.github.chainfs.ecc2;

import java.math.BigInteger;

public class TestSecp256k1 {
    public static void main(String[] args) {
        Secp256k1 curve = new Secp256k1();

        // Generator point of secp256k1
        BigInteger Gx = new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
        BigInteger Gy = new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
        BigInteger[] G = new BigInteger[] { Gx, Gy };

        System.out.println("G: " + Secp256k1.pointToString(G));
        System.out.println("On curve? " + curve.isOnCurve(G));

        BigInteger[] G2 = curve.multiply(G, BigInteger.TWO);
        System.out.println("2G: " + Secp256k1.pointToString(G2));
        System.out.println("On curve? " + curve.isOnCurve(G2));

        // Try division polynomial ψ3(x) for Gx
        BigInteger psi3 = curve.divisionPolynomial3(Gx);
        System.out.println("ψ₃(Gx): " + psi3.toString(16));

        BigInteger psi5 = curve.divisionPolynomial(Gx, 5);
        System.out.println("ψ₅(Gx): " + psi5.toString(16));

        BigInteger psi7 = curve.divisionPolynomial(Gx, 7);
        System.out.println("ψ₇(Gx): " + psi7.toString(16));
    }
}
/*

₀ (U+2080)

₁ (U+2081)

₂ (U+2082)

₃ (U+2083)

₄ (U+2084)

₅ (U+2085)

₆ (U+2086)

₇ (U+2087)

₈ (U+2088)

₉*/