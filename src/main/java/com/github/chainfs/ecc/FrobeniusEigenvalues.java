package com.github.chainfs.ecc;
import java.math.BigInteger;

public class FrobeniusEigenvalues {

    // Solve quadratic x^2 - t x + p ≡ 0 (mod l)
    // Returns roots as BigInteger[] (length 2) or null if no roots.
    public static BigInteger[] solveFrobeniusEigenvalues(BigInteger t, BigInteger p, BigInteger l) {
        // Characteristic polynomial: x^2 - t x + p ≡ 0 mod l
        // Compute discriminant = t^2 - 4p mod l
        BigInteger four = BigInteger.valueOf(4);
        BigInteger discriminant = t.multiply(t).subtract(four.multiply(p)).mod(l);

        // Compute sqrt(discriminant) mod l
        BigInteger sqrtDisc = modSqrt(discriminant, l);
        if (sqrtDisc == null) {
            // No solution: polynomial doesn't split mod l (shouldn't happen for Elkies primes)
            return null;
        }

        // Compute inverse of 2 mod l
        BigInteger two = BigInteger.valueOf(2);
        BigInteger twoInv = two.modInverse(l);

        // Roots: (t ± sqrt(discriminant)) / 2 mod l
        BigInteger root1 = t.add(sqrtDisc).multiply(twoInv).mod(l);
        BigInteger root2 = t.subtract(sqrtDisc).multiply(twoInv).mod(l);

        return new BigInteger[] { root1, root2 };
    }

    // Tonelli-Shanks algorithm or similar to compute sqrt modulo prime l
    // Returns null if no sqrt exists.
    public static BigInteger modSqrt(BigInteger n, BigInteger p) {
        if (p.equals(BigInteger.TWO)) return n.mod(p);
        if (legendreSymbol(n, p) != 1) return null; // No sqrt if not a quadratic residue

        BigInteger q = p.subtract(BigInteger.ONE);
        int s = 0;
        while (q.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            q = q.divide(BigInteger.TWO);
            s++;
        }

        if (s == 1) {
            BigInteger r = n.modPow(p.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), p);
            return r;
        }

        // Find a quadratic non-residue z
        BigInteger z = BigInteger.TWO;
        while (legendreSymbol(z, p) != -1) {
            z = z.add(BigInteger.ONE);
        }

        BigInteger c = z.modPow(q, p);
        BigInteger r = n.modPow(q.add(BigInteger.ONE).divide(BigInteger.TWO), p);
        BigInteger t = n.modPow(q, p);
        int m = s;

        while (!t.equals(BigInteger.ONE)) {
            BigInteger tt = t;
            int i = 0;
            while (!tt.equals(BigInteger.ONE)) {
                tt = tt.multiply(tt).mod(p);
                i++;
                if (i == m) return null; // Shouldn't happen
            }

            BigInteger b = c.modPow(BigInteger.valueOf(1L << (m - i - 1)), p);
            r = r.multiply(b).mod(p);
            c = b.multiply(b).mod(p);
            t = t.multiply(c).mod(p);
            m = i;
        }

        return r;
    }

    // Compute Legendre symbol (a|p), returns 1 if a is quadratic residue mod p, -1 if non-residue, 0 if a ≡ 0 mod p
    public static int legendreSymbol(BigInteger a, BigInteger p) {
        BigInteger ls = a.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.TWO), p);
        if (ls.equals(BigInteger.ONE)) return 1;
        if (ls.equals(p.subtract(BigInteger.ONE))) return -1;
        return 0;
    }

    public static void main(String[] args) {
    	compute(null);
    }

    public static BigInteger[] compute(BigInteger l) {

        BigInteger p =
                new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F",
         	   16);

        if (l == null) {
        	l = new BigInteger("5");   // Elkies prime
        }
        System.out.println("Elkies prime = " + l);

        // Suppose t mod l is known (you need to find it)
        BigInteger t = new BigInteger("14551231950b75fc4402da1722fc9baef", 16);
        System.out.println("Is t the constant t or t mod l?, constant: " + t);
        BigInteger tModl = t.mod(l);
		System.out.println("Is t the constant t or t mod l?, t.mod(l): " + tModl);

        BigInteger[] roots = solveFrobeniusEigenvalues(t, p, l);
        if (roots != null) {
            System.out.println("Eigenvalues mod " + l + ": " + roots[0] + ", " + roots[1]);
            return new BigInteger[] {l, roots[0], roots[1]};
        } else {
            System.out.println("No eigenvalues found modulo " + l);
            return new BigInteger[] {l};
        }
    }
}
