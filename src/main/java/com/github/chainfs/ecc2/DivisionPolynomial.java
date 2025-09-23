package com.github.chainfs.ecc2;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class DivisionPolynomial {

    // secp256k1 prime
    private static final BigInteger p = new BigInteger(
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger THREE = BigInteger.valueOf(3);

    // Cache polynomials ψ_n(x) for memoization (only x, so BigInteger representing value at given x)
    private Map<Integer, BigInteger> cache = new HashMap<>();

    private final BigInteger x;

    public DivisionPolynomial(BigInteger x) {
        this.x = x.mod(p);
    }

    // Compute ψ_n(x) recursively for odd n >=1
    public BigInteger psi(int n) {
        if (cache.containsKey(n)) return cache.get(n);

        BigInteger result;

        if (n == 0) {
            result = ZERO;
        } else if (n == 1) {
            result = ONE;
        } else if (n == 2) {
            // ψ₂ = 2y but y not known here; we'll return null or throw as it depends on y
            // For this class, only compute for odd n (where y not needed)
            throw new UnsupportedOperationException("ψ₂ depends on y, cannot compute with x alone");
        } else if (n == 3) {
            // ψ₃(x) = 3x^4 + 6ax^2 + 12bx - a²
            // secp256k1: a=0, b=7, so simplifies:
            // ψ₃(x) = 3x^4 + 12*7*x = 3x^4 + 84x
            BigInteger term1 = THREE.multiply(x.pow(4)).mod(p);
            BigInteger term2 = BigInteger.valueOf(84).multiply(x).mod(p);
            result = term1.add(term2).mod(p);
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
            // ψ_{2m} = (ψ_m / 2y) * (ψ_{m+2} * ψ_{m-1}² - ψ_{m-2} * ψ_{m+1}²)
            // Depends on y, cannot compute with x alone; throw for now
            throw new UnsupportedOperationException("ψ_even depends on y, cannot compute with x alone");
        }

        cache.put(n, result);
        return result;
    }

    public static void main(String[] args) {
        BigInteger Gx = new BigInteger(
            "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16);
        DivisionPolynomial dp = new DivisionPolynomial(Gx);

        int[] primes = {3, 5, 7, 11, 13};
        for (int l : primes) {
            try {
                BigInteger psi_l = dp.psi(l);
                System.out.println("ψ_" + l + "(Gx) = " + psi_l.toString(16));
            } catch (UnsupportedOperationException e) {
                System.out.println("ψ_" + l + "(x) cannot be computed fully with x alone: " + e.getMessage());
            }
        }
    }
}
