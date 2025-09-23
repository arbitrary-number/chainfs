package com.github.chainfs.ecc2;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class DivisionPolynomialWithY {

    // secp256k1 prime field
    private static final BigInteger p = new BigInteger(
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger THREE = BigInteger.valueOf(3);

    // Curve parameters for secp256k1 y^2 = x^3 + 7
    private static final BigInteger a = BigInteger.ZERO;
    private static final BigInteger b = BigInteger.valueOf(7);

	private static final BigInteger FOUR = new BigInteger("4");

    private final BigInteger x;
    private final BigInteger y;

    // Cache for memoization: n -> ψₙ(x,y)
    private final Map<Integer, BigInteger> cache = new HashMap<>();

    public DivisionPolynomialWithY(BigInteger x, BigInteger y) {
        this.x = x.mod(p);
        this.y = y.mod(p);
    }

    public BigInteger psi(int n) {
    	//System.out.println("recursive n: " + n);
    	if (n < 0) {
            // By definition or convention ψ_n = 0 for negative n
            return ZERO;
        }
        if (cache.containsKey(n)) return cache.get(n);

        BigInteger result;

        if (n == 0) {
            result = ZERO;
        } else if (n == 1) {
            result = ONE;
        } else if (n == 2) {
            // ψ₂ = 2y
            result = TWO.multiply(y).mod(p);
        } else if (n == 3) {
            // ψ₃ = 3x^4 + 6ax^2 + 12bx - a^2
            // For secp256k1: a=0, b=7
            BigInteger term1 = THREE.multiply(x.pow(4)).mod(p);
            BigInteger term2 = BigInteger.valueOf(12).multiply(b).multiply(x).mod(p);
            result = term1.add(term2).mod(p);
        } else if (n == 4) {
                BigInteger x3 = x.pow(3).mod(p);
                BigInteger x6 = x3.pow(2).mod(p);
                BigInteger term = x6.add(BigInteger.valueOf(140).multiply(x3)).subtract(BigInteger.valueOf(392)).mod(p);
                result = FOUR.multiply(y).multiply(term).mod(p);
        } else if (n % 2 == 1) {
            // Odd n > 3:
            // ψ_{2m+1} = ψ_{m+2} * ψ_m^3 - ψ_{m-1} * ψ_{m+1}^3
            int m = (n - 1) / 2;
            BigInteger psi_m2 = psi(m + 2);
            BigInteger psi_m = psi(m);
            BigInteger psi_m1 = psi(m - 1);
            BigInteger psi_mplus1 = psi(m + 1);

            BigInteger term1 = psi_m2.multiply(psi_m.pow(3)).mod(p);
            BigInteger term2 = psi_m1.multiply(psi_mplus1.pow(3)).mod(p);

            result = term1.subtract(term2).mod(p);
            if (result.signum() < 0) {
                result = result.add(p);
            }
        } else {
            // Even n:
            // ψ_{2m} = (ψ_m / (2y)) * (ψ_{m+2} * ψ_{m-1}^2 - ψ_{m-2} * ψ_{m+1}^2)
            int m = n / 2;
            BigInteger psi_m = psi(m);
            BigInteger psi_m2 = psi(m + 2);
            BigInteger psi_m1 = psi(m - 1);
            BigInteger psi_m_2 = psi(m - 2);
            BigInteger psi_mplus1 = psi(m + 1);

            BigInteger numerator = psi_m2.multiply(psi_m1.pow(2)).mod(p)
                                .subtract(psi_m_2.multiply(psi_mplus1.pow(2))).mod(p);

            // Modular inverse of 2y mod p
            BigInteger inv2y = TWO.multiply(y).modInverse(p);

            result = psi_m.multiply(numerator).mod(p);
            result = result.multiply(inv2y).mod(p);

            if (result.signum() < 0) {
                result = result.add(p);
            }
        }

        cache.put(n, result);
        return result;
    }

    public static void main(String[] args) {
        BigInteger Gx = new BigInteger(
            "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16);
        BigInteger Gy = new BigInteger(
            "483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16);

        DivisionPolynomialWithY dp = new DivisionPolynomialWithY(Gx, Gy);

        int[] tests = {2, 3, 4, 5, 6, 7, 11, 13, 17, 19, 23, 29, 31, 33, 37, 39, 41, 43, 47, 51,
        		53, 57, 59, 63, 67, 71, 73, 79, 83, 87, 89, 91, 93, 97, 99, 100, 1000, 10000};

        for (int n : tests) {
            BigInteger psi_n = dp.psi(n);
            System.out.printf("ψ_%d(G) = %s%n", n, psi_n.toString(16));
        }
    }
}
