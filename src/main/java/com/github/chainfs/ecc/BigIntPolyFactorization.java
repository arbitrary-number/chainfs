package com.github.chainfs.ecc;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BigIntPolyFactorization {

    static SecureRandom random = new SecureRandom();

    // =========== Polynomial arithmetic with BigInteger coefficients mod p ===========

    // Add two polynomials mod p
    static BigInteger[] polyAdd(BigInteger[] a, BigInteger[] b, BigInteger p) {
        int n = Math.max(a.length, b.length);
        BigInteger[] res = new BigInteger[n];
        for (int i = 0; i < n; i++) {
            BigInteger ai = i < a.length ? a[i] : BigInteger.ZERO;
            BigInteger bi = i < b.length ? b[i] : BigInteger.ZERO;
            res[i] = ai.add(bi).mod(p);
        }
        return trim(res);
    }

    // Subtract b from a mod p
    static BigInteger[] polySub(BigInteger[] a, BigInteger[] b, BigInteger p) {
        int n = Math.max(a.length, b.length);
        BigInteger[] res = new BigInteger[n];
        for (int i = 0; i < n; i++) {
            BigInteger ai = i < a.length ? a[i] : BigInteger.ZERO;
            BigInteger bi = i < b.length ? b[i] : BigInteger.ZERO;
            res[i] = ai.subtract(bi).mod(p);
        }
        return trim(res);
    }

    // Multiply two polynomials mod p
    static BigInteger[] polyMul(BigInteger[] a, BigInteger[] b, BigInteger p) {
        BigInteger[] res = new BigInteger[a.length + b.length - 1];
        for (int i = 0; i < res.length; i++) res[i] = BigInteger.ZERO;

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                res[i + j] = res[i + j].add(a[i].multiply(b[j])).mod(p);
            }
        }
        return trim(res);
    }

    // Polynomial modulo: a mod b
    static BigInteger[] polyMod(BigInteger[] a, BigInteger[] b, BigInteger p) {
        BigInteger[] r = a.clone();
        int degB = deg(b);
        BigInteger invLead = b[degB].modInverse(p);

        for (int i = r.length - 1; i >= degB; i--) {
            if (r[i].equals(BigInteger.ZERO)) continue;
            BigInteger coef = r[i].multiply(invLead).mod(p);
            for (int j = 0; j <= degB; j++) {
                r[i - degB + j] = r[i - degB + j].subtract(coef.multiply(b[j])).mod(p);
            }
        }
        int degR = deg(r);
        BigInteger[] res = new BigInteger[degR + 1];
        System.arraycopy(r, 0, res, 0, degR + 1);
        return trim(res);
    }

    // Polynomial GCD mod p
    static BigInteger[] polyGCD(BigInteger[] a, BigInteger[] b, BigInteger p) {
        while (!isZero(b)) {
            BigInteger[] r = polyMod(a, b, p);
            a = b;
            b = r;
        }
        // Normalize leading coefficient to 1
        BigInteger invLead = a[a.length - 1].modInverse(p);
        for (int i = 0; i < a.length; i++) {
            a[i] = a[i].multiply(invLead).mod(p);
        }
        return a;
    }

    // Polynomial division: f / g assuming g divides f exactly
    static BigInteger[] polyDiv(BigInteger[] f, BigInteger[] g, BigInteger p) {
        int degF = deg(f);
        int degG = deg(g);
        BigInteger[] r = f.clone();
        BigInteger[] q = new BigInteger[degF - degG + 1];
        for (int i = 0; i < q.length; i++) q[i] = BigInteger.ZERO;

        BigInteger invLead = g[degG].modInverse(p);

        for (int i = degF; i >= degG; i--) {
            BigInteger coef = r[i].multiply(invLead).mod(p);
            q[i - degG] = coef;
            for (int j = 0; j <= degG; j++) {
                r[i - degG + j] = r[i - degG + j].subtract(coef.multiply(g[j])).mod(p);
            }
        }
        return trim(q);
    }

    // Polynomial exponentiation mod f: base^exp mod f, mod p
    static BigInteger[] polyPowMod(BigInteger[] base, BigInteger exp, BigInteger[] f, BigInteger p) {
        BigInteger[] result = new BigInteger[]{BigInteger.ONE};
        BigInteger[] cur = base.clone();

        while (exp.signum() > 0) {
            if (exp.testBit(0)) {
                result = polyMod(polyMul(result, cur, p), f, p);
            }
            cur = polyMod(polyMul(cur, cur, p), f, p);
            exp = exp.shiftRight(1);
        }
        return result;
    }

    // Degree of polynomial
    static int deg(BigInteger[] poly) {
        for (int i = poly.length - 1; i >= 0; i--) {
            if (!poly[i].equals(BigInteger.ZERO)) return i;
        }
        return -1;
    }

    // Trim leading zeros
    static BigInteger[] trim(BigInteger[] poly) {
        int d = deg(poly);
        if (d == -1) return new BigInteger[]{BigInteger.ZERO};
        BigInteger[] res = new BigInteger[d + 1];
        System.arraycopy(poly, 0, res, 0, d + 1);
        return res;
    }

    // Check zero polynomial
    static boolean isZero(BigInteger[] poly) {
        return deg(poly) == -1 || (deg(poly) == 0 && poly[0].equals(BigInteger.ZERO));
    }

    // Generate random polynomial degree < degBound
    static BigInteger[] randomPoly(int degBound, BigInteger p) {
        int deg = random.nextInt(degBound) + 1;
        BigInteger[] poly = new BigInteger[deg + 1];
        for (int i = 0; i < deg; i++) {
            poly[i] = new BigInteger(p.bitLength(), random).mod(p);
        }
        poly[deg] = BigInteger.ONE; // Ensure degree == deg
        return trim(poly);
    }


    // =========== Distinct Degree Factorization (DDF) ===========

    // Returns a list of polynomials, each product of irreducibles of same degree
    static List<BigInteger[]> distinctDegreeFactorization(BigInteger[] f, BigInteger p) {
        List<BigInteger[]> factors = new ArrayList<>();

        BigInteger[] R = f.clone();
        int degR = deg(R);
        int d = 1;
        BigInteger[] xPoly = new BigInteger[]{BigInteger.ZERO, BigInteger.ONE};

        while (degR >= 2 * d) {
            // Compute x^{p^d} mod R
            BigInteger[] xp = polyPowModFrobenius(xPoly, p, d, R, p);

            // Compute gcd(R, xp - x)
            BigInteger[] diff = polySub(xp, xPoly, p);
            BigInteger[] g = polyGCD(R, diff, p);

            if (deg(g) > 0) {
                factors.add(g);
                R = polyDiv(R, g, p);
                degR = deg(R);
            }
            d++;
        }
        if (degR > 0) factors.add(R);
        return factors;
    }

    // Compute x^{p^d} mod f using Frobenius map properties
    //original with bug
//    static BigInteger[] polyPowModFrobenius(BigInteger[] base, BigInteger p, int d, BigInteger[] f, BigInteger prime) {
//        BigInteger[] result = base.clone();
//
//        for (int i = 0; i < d; i++) {
//            // Raise coefficients to p-th power mod prime
//            for (int j = 0; j < result.length; j++) {
//                result[j] = result[j].modPow(p, prime);
//            }
//            // Raise polynomial to p-th power mod f
//            result = polyPowMod(result, prime, f, prime);
//        }
//        return result;
//    }

    static BigInteger[] polyPowModFrobenius(BigInteger[] poly, BigInteger p, int d, BigInteger[] f, BigInteger prime) {
        BigInteger[] result = poly.clone();
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < result.length; j++) {
                result[j] = result[j].modPow(p, prime);
            }
            // No further polynomial exponentiation needed here
            result = polyMod(result, f, prime);
        }
        return result;
    }


    // =========== Cantor-Zassenhaus Factorization ===========

    // Cantor-Zassenhaus split: tries to find nontrivial factor of f whose irreducibles have degree d
    static BigInteger[] cantorZassenhausSplit(BigInteger[] f, int d, BigInteger p) {
        int n = deg(f);
        BigInteger qdMinus1 = p.pow(d).subtract(BigInteger.ONE);
        BigInteger exp = qdMinus1.shiftRight(1); // (q^d -1)/2

        for (int tries = 0; tries < 30; tries++) {
            BigInteger[] a = randomPoly(n, p);
            BigInteger[] aPow = polyPowMod(a, exp, f, p);
            BigInteger[] g = polyGCD(f, polySub(aPow, new BigInteger[]{BigInteger.ONE}, p), p);

            int degG = deg(g);
            if (degG > 0 && degG < n) {
                return g;
            }
        }
        return null;
    }

    // Recursively factor f whose irreducible factors have degree d
    static List<BigInteger[]> factorCZ(BigInteger[] f, int d, BigInteger p) {
        List<BigInteger[]> factors = new ArrayList<>();
        int degF = deg(f);
        if (degF == d) {
            factors.add(f);
            return factors;
        }

        while (true) {
            BigInteger[] factor = cantorZassenhausSplit(f, d, p);
            if (factor == null) {
                // Failed to find nontrivial factor — f is irreducible or already prime power
                factors.add(f);
                break;
            }
            f = polyDiv(f, factor, p);
            factors.addAll(factorCZ(factor, d, p));
            if (deg(f) == 0) break;  // completely factored
        }
        return factors;
    }

    public static List<BigInteger[]> fullFactorization(BigInteger[] f, BigInteger p) {
        List<BigInteger[]> factors = new ArrayList<>();

        // First, run DDF to get product of irreducibles of fixed degrees
        List<BigInteger[]> ddfFactors = distinctDegreeFactorization(f, p);

        // For each factor (which contains irreducibles of same degree), run CZ
        for (BigInteger[] factor : ddfFactors) {
            int degFactor = deg(factor);
            if (degFactor <= 1) {
                factors.add(factor);
                continue;
            }
            int d = findIrreducibleDegree(factor, p); // usually degree divides factor's degree
            factors.addAll(factorCZ(factor, d, p));
        }
        return factors;
    }

    // A simple heuristic to find degree of irreducible factors in factor:
    static int findIrreducibleDegree(BigInteger[] f, BigInteger p) {
        int degF = deg(f);
        for (int d = 1; d <= degF; d++) {
            // If x^{p^d} - x mod f == 0, degree divides d
            BigInteger[] xPoly = new BigInteger[]{BigInteger.ZERO, BigInteger.ONE};
            BigInteger[] xp = polyPowModFrobenius(xPoly, p, d, f, p);
            BigInteger[] diff = polySub(xp, xPoly, p);
            BigInteger[] g = polyGCD(f, diff, p);
            if (deg(g) == degF) {
                return d;
            }
        }
        return degF; // fallback
    }

    // Helper to convert a polynomial to hex string
    static String polyToHex(BigInteger[] poly) {
        StringBuilder sb = new StringBuilder();
        for (int i = poly.length - 1; i >= 0; i--) {
            sb.append(poly[i].toString(16));
            if (i > 0) sb.append("x^").append(i).append(" + ");
        }
        return sb.toString();
    }

    static String polyToHexUTF8(BigInteger[] poly) {
        StringBuilder sb = new StringBuilder();
        for (int i = poly.length - 1; i >= 0; i--) {
            if (poly[i].equals(BigInteger.ZERO)) continue;

            String coefHex = poly[i].toString(16);
            if (i == 0) {
                sb.append(coefHex);
            } else {
                sb.append(coefHex).append("x");
                sb.append(toSuperscript(i));
            }

            if (i > 0) sb.append(" + ");
        }
        return sb.toString();
    }

    // Convert an integer exponent to UTF-8 superscript string
    static String toSuperscript(int exponent) {
        final String[] superscripts = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};
        StringBuilder sb = new StringBuilder();
        for (char c : Integer.toString(exponent).toCharArray()) {
            sb.append(superscripts[c - '0']);
        }
        return sb.toString();
    }

    static String polyToHexUTF8Smart(BigInteger[] poly) {
        StringBuilder sb = new StringBuilder();
        boolean firstTerm = true;

        for (int i = poly.length - 1; i >= 0; i--) {
            BigInteger coef = poly[i];
            if (coef.equals(BigInteger.ZERO)) continue;

            if (!firstTerm) sb.append(" + ");

            String coefHex = coef.toString(16);

            if (i == 0) {
                // Constant term: always include coefficient
                sb.append(coefHex);
            } else {
                // For x terms
                if (!coef.equals(BigInteger.ONE)) {
                    sb.append(coefHex);
                }

                sb.append("x");

                if (i > 1) {
                    sb.append(toSuperscript(i));
                }
            }

            firstTerm = false;
        }

        if (firstTerm) return "0"; // all terms were zero

        return sb.toString();
    }

    public static void printPoly(BigInteger[] f) {
        StringBuilder sb = new StringBuilder("f(x) = ");
        boolean firstTerm = true;

        for (int i = f.length - 1; i >= 0; i--) {
            BigInteger coeff = f[i];
            if (coeff.equals(BigInteger.ZERO)) continue;

            // Print + between terms except before first printed term
            if (!firstTerm) {
                sb.append(" + ");
            }

            boolean isCoeffOne = coeff.equals(BigInteger.ONE);
            boolean isConstantTerm = (i == 0);

            if (isConstantTerm) {
                // Just print the coefficient
                sb.append(coeff.toString());
            } else {
                // For non-constant terms, omit coefficient if 1
                if (!isCoeffOne) {
                    sb.append(coeff.toString());
                }
                sb.append("x");
                sb.append(toSuperscript(i));
            }

            firstTerm = false;
        }

        // If no terms printed, print 0
        if (firstTerm) {
            sb.append("0");
        }

        System.out.println(sb.toString());
    }

    public static void printFactorsProduct(List<BigInteger[]> factors) {
        StringBuilder sb = new StringBuilder("f(x) = ");
        for (BigInteger[] factor : factors) {
            sb.append("(");
            sb.append(polyToHexUTF8Smart(factor));
            sb.append(")");
        }
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
    	compute(null, null);
    }

    public static BigInteger[] compute(BigInteger[] f, BigInteger l) {

        BigInteger p =
                new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F",
         	   16);

         // t is normally unknown when creating secp256k1
         BigInteger t = new BigInteger("14551231950b75fc4402da1722fc9baef", 16);

         System.out.println("t (normally unknown but could possibly iterate/binary search for it) = " + t.toString(16));

    	//String[] lArray = new String[] {"2", "3", "5", "7", "11", "13"};

    	if (l == null) {
    		l = new BigInteger("3");
    	}
    	System.out.println("ℓ = " + l);   // strange l is a low prime

    	//System.out.println("f(x) = x²");
    	if (f == null) {
    		f = PolyBuilder.buildFl(l, t, p);
//    		f = new BigInteger[] {
//    				new BigInteger("0"),          // constant term
//    				BigInteger.ZERO,   // 0x
//    				BigInteger.ONE,         // x^2 term
//    		};
    	}

        System.out.println("---Print poly: ---");
        printPoly(f);

    	// Polynomial: f(x) = x^3 + 0x + 7 mod p over F_p  secp256k1
//        BigInteger[] f = new BigInteger[] {
//    	            new BigInteger("7"),          // constant term
//    	            BigInteger.ZERO,   // 2x
//    	            BigInteger.ZERO,         // x^2 term
//    	            BigInteger.ONE           // x^3 term
//	    };

        // Example polynomial over F_p:
        //BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16); // secp256k1 prime

        System.out.println("p = " + p.toString(16));
//        // Example polynomial: f(x) = x^3 + 2x + 1 mod p
//        BigInteger[] f = new BigInteger[] {
//            BigInteger.ONE,                    // constant term 1
//            BigInteger.valueOf(2),             // x term 2
//            BigInteger.ZERO,                   // x^2 term 0
//            BigInteger.ONE                    // x^3 term 1
//        };

        List<BigInteger[]> factors = fullFactorization(f, l);  //p);

        printFactorsProduct(factors);
        // this does work, so looks like large numbers can be factored
        // List<BigInteger[]> factors = fullFactorization(f, p);

//        System.out.println("Factors:");
//        for (BigInteger[] factor : factors) {
//            System.out.print("Degree " + deg(factor) + ": ");
//            for (int i = deg(factor); i >= 0; i--) {
//                System.out.print(factor[i].toString(16) + (i > 0 ? "x^" + i + " + " : ""));
//            }
//            System.out.println();
//        }
//        System.out.println("\n---- Using helper: -----");
//        System.out.println("Factors:");
//        for (BigInteger[] factor : factors) {
//            System.out.println("Degree " + deg(factor) + ": " + polyToHex(factor));
//        }
//        System.out.println("\n---- Factors UTF-8: ----");
//        for (BigInteger[] factor : factors) {
//            System.out.println("Degree " + deg(factor) + ": " + polyToHexUTF8(factor));
//        }
        System.out.println("\n---- Smart Factors UTF-8 -----:");
        ArrayList<BigInteger> factorDegrees = new ArrayList();
        for (BigInteger[] factor : factors) {
            int deg = deg(factor);
			System.out.println("Degree " + deg + ": " + polyToHexUTF8Smart(factor));
            factorDegrees.add(new BigInteger(String.valueOf(deg)));
        }

        boolean elkies = false;
        for (BigInteger degree : factorDegrees) {
            if (degree.intValue() == 1) {
                elkies = true;
                break;
            }
        }

        boolean hasRepeatedFactor = false;
        for (int i = 0; i < factors.size() - 1; i++) {
            for (int j = i + 1; j < factors.size(); j++) {
                if (Arrays.equals(factors.get(i), factors.get(j))) {
                    hasRepeatedFactor = true;
                    break;
                }
            }
            if (hasRepeatedFactor) break;
        }

        if (hasRepeatedFactor) {
            System.out.println("Repeated factors found; ℓ = " + l + " should be considered Atkin.");
            elkies = false;
        }

        //ArrayList<BigInteger> elkiesList = new ArrayList<BigInteger>();

        if (elkies) {
            System.out.println("ℓ = " + l + " is an Elkies prime (not Atkin)");
            BigInteger elkie = l;
            if (elkie.compareTo(new BigInteger("2")) == 0) {
            	System.out.println("Skipping trivial elkie of: " + elkie);
            	return null;
            }
            return FrobeniusEigenvalues.compute(elkie);
        } else {
            System.out.println("ℓ = " + l + " is an Atkin prime (not Elkies)");
            return null;
        }
    }
}


