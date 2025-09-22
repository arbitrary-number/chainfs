package com.github.chainfs.ecc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.security.SecureRandom;
import java.util.HashMap;

public class Secp256k1EC {

    private static final int LOW_BIT_COUNT_FOR_PRECOMPUTE = 1;

	private static final int MAX_DEPTH = 20 - LOW_BIT_COUNT_FOR_PRECOMPUTE;

	private static final BigInteger TWO = BigInteger.valueOf(2);

	// secp256k1 field modulus p = 2^256 - 2^32 - 977
    static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    private static final BigInteger CURVE_ORDER = new BigInteger(
    	    "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

	private static final boolean SQUARES_AND_CUBES = false;

	private static HashMap<String, BigInteger> lowBitPrecompute =
			new HashMap<String, BigInteger>();

    // Helper: modulo with wraparound
    static BigInteger mod(BigInteger x) {
        x = x.mod(P);
        return x.signum() < 0 ? x.add(P) : x;
    }

    static BigInteger modAdd(BigInteger a, BigInteger b) {
        return mod(a.add(b));
    }

    static BigInteger modSub(BigInteger a, BigInteger b) {
        return mod(a.subtract(b));
    }

    static BigInteger modMul(BigInteger a, BigInteger b) {
        return mod(a.multiply(b));
    }

    static BigInteger modSqr(BigInteger a) {
        return mod(a.multiply(a));
    }

	public static BigInteger[] pointDoubleWithWrapCount(BigInteger X1, BigInteger Y1, BigInteger Z1,
            MutableCount[] wrapCount) {

        BigInteger A = modSqrTrack(X1, P, wrapCount[0]);                             // A = X1^2
        BigInteger B = modSqrTrack(Y1, P, wrapCount[1]);                             // B = Y1^2
        BigInteger C = modSqrTrack(B, P, wrapCount[2]);                              // C = B^2

        BigInteger D = modMulTrack(BigInteger.valueOf(4), modMulTrack(X1, B, P, wrapCount[3]), P, wrapCount[4]); // D = 4 * X1 * B
        BigInteger E = modMulTrack(BigInteger.valueOf(3), A, P, wrapCount[5]);             // E = 3 * A
        BigInteger F = modSqrTrack(E, P, wrapCount[6]);                                    // F = E^2

        BigInteger X3 = modSubTrack(F, modMulTrack(TWO, D, P, wrapCount[7]), P, wrapCount[8]); // X3 = F - 2*D
        BigInteger Y3 = modSubTrack(modMulTrack(E, modSubTrack(D, X3, P, wrapCount[9]), P, wrapCount[10]),
        		modMulTrack(BigInteger.valueOf(8), C, P, wrapCount[11]), P, wrapCount[12]); // Y3 = E*(D - X3) - 8*C
        BigInteger Z3 = modMulTrack(TWO, modMulTrack(Y1, Z1, P, wrapCount[13]), P, wrapCount[14]); // Z3 = 2 * Y1 * Z1

        return new BigInteger[]{X3, Y3, Z3};
	}

    public static BigInteger[] pointDoubleJac(BigInteger X1, BigInteger Y1, BigInteger Z1) {
    	return pointDouble(X1, Y1, Z1);
    }

    // Point doubling in Jacobian coordinates
    public static BigInteger[] pointDouble(BigInteger X1, BigInteger Y1, BigInteger Z1) {
        BigInteger A = modSqr(X1);                             // A = X1^2
        BigInteger B = modSqr(Y1);                             // B = Y1^2
        BigInteger C = modSqr(B);                              // C = B^2

        BigInteger D = modMul(BigInteger.valueOf(4), modMul(X1, B)); // D = 4 * X1 * B
        BigInteger E = modMul(BigInteger.valueOf(3), A);             // E = 3 * A
        BigInteger F = modSqr(E);                                    // F = E^2

        BigInteger X3 = modSub(F, modMul(TWO, D)); // X3 = F - 2*D
        BigInteger Y3 = modSub(modMul(E, modSub(D, X3)), modMul(BigInteger.valueOf(8), C)); // Y3 = E*(D - X3) - 8*C
        BigInteger Z3 = modMul(TWO, modMul(Y1, Z1)); // Z3 = 2 * Y1 * Z1

        return new BigInteger[]{X3, Y3, Z3};
    }

    // Points are Jacobian
    public static Point add(Point P1, Point Q, Residue residue) {
        if (P1.isInfinity()) return Q;
        if (Q.isInfinity()) return P1;

        BigInteger Z1Z1 = P1.Z.multiply(P1.Z).mod(P);
        BigInteger Z2Z2 = Q.Z.multiply(Q.Z).mod(P);

        BigInteger U1 = P1.X.multiply(Z2Z2).mod(P);
        BigInteger U2 = Q.X.multiply(Z1Z1).mod(P);

        BigInteger Z1Cubed = P1.Z.multiply(Z1Z1).mod(P);
        BigInteger Z2Cubed = Q.Z.multiply(Z2Z2).mod(P);

        BigInteger S1 = P1.Y.multiply(Z2Cubed).mod(P);
        BigInteger S2 = Q.Y.multiply(Z1Cubed).mod(P);

        if (U1.equals(U2)) {
            if (S1.equals(S2)) {
                return P1.doublePoint();
            } else {
                return new Point(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO); // point at infinity
            }
        }

        BigInteger H = U2.subtract(U1).mod(P);
        BigInteger R = S2.subtract(S1).mod(P);
        BigInteger H2 = H.multiply(H).mod(P);
        BigInteger H3 = H2.multiply(H).mod(P);
        BigInteger U1H2 = U1.multiply(H2).mod(P);

        BigInteger X3 = R.multiply(R).subtract(H3).subtract(U1H2.multiply(BigInteger.TWO)).mod(P);
        BigInteger Y3 = R.multiply(U1H2.subtract(X3)).subtract(S1.multiply(H3)).mod(P);
        BigInteger Z3 = H.multiply(P1.Z).multiply(Q.Z).mod(P);

        // Residue info
        residue.U1 = legendre(U1);
        residue.U2 = legendre(U2);
        residue.S1 = legendre(S1);
        residue.S2 = legendre(S2);
        residue.H = legendre(H);
        residue.R = legendre(R);
		System.out.printf("Residues: U1=%d, U2=%d, S1=%d, S2=%d, H=%d, R=%d%n",
				residue.U1, residue.U2, residue.S1, residue.S2, residue.H, residue.R);

        return new Point(X3, Y3, Z3);
    }

    static int legendre(BigInteger a) {
        if (a.equals(BigInteger.ZERO)) return 0;
        return a.modPow(P.subtract(BigInteger.ONE).divide(BigInteger.TWO), P).equals(BigInteger.ONE) ? 1 : 0;
    }

    // Mixed point addition (Jacobian + affine), returns Jacobian
    public static BigInteger[] pointAddMixed(BigInteger X1, BigInteger Y1, BigInteger Z1,
                                             BigInteger x2, BigInteger y2) {
        BigInteger Z1Z1 = modSqr(Z1);
        BigInteger U2 = modMul(x2, Z1Z1);
        BigInteger S2 = modMul(y2, modMul(Z1, Z1Z1));
        BigInteger H = modSub(U2, X1);
        BigInteger r = modSub(S2, Y1);

        if (H.signum() == 0) {
            if (r.signum() == 0) {
                return pointDouble(X1, Y1, Z1); // P == Q
            } else {
                return new BigInteger[]{BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO}; // Point at infinity
            }
        }

        BigInteger H2 = modSqr(H);
        BigInteger H3 = modMul(H, H2);
        BigInteger V = modMul(X1, H2);

        BigInteger X3 = modSub(modSub(modSqr(r), H3), modMul(TWO, V));
        BigInteger Y3 = modSub(modMul(r, modSub(V, X3)), modMul(Y1, H3));
        BigInteger Z3 = modMul(Z1, H);

        return new BigInteger[]{X3, Y3, Z3};
    }

    public static BigInteger modMulTrack(BigInteger a, BigInteger b, BigInteger mod, MutableCount count) {
        BigInteger result = a.multiply(b);
        BigInteger[] divRem = result.divideAndRemainder(mod);
        count.count = result;  //divRem[0];
        return divRem[1];
    }

    // Modular squaring with wrap tracking
    public static BigInteger modSqrTrack(BigInteger a, BigInteger mod, MutableCount count) {
        return modMulTrack(a, a, mod, count);
    }

    // Modular subtraction with wrap tracking
    public static BigInteger modSubTrack(BigInteger a, BigInteger b, BigInteger mod, MutableCount count) {
        BigInteger result = a.subtract(b);
        if (result.signum() < 0) {
            result = result.add(mod);
            count.count = BigInteger.ONE;  // One wrap occurred due to negative result
        }
        return result.mod(mod);  // Just to be safe
    }

    static class MutableCount {
    	public BigInteger count = BigInteger.ZERO;
    }

	public static BigInteger[] pointAddMixedWithWrapCount(BigInteger X1, BigInteger Y1, BigInteger Z1,
	            BigInteger x2, BigInteger y2,
	            MutableCount[] wrapCount) {
		final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
		final BigInteger N = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

		BigInteger Z1Z1 = modSqrTrack(Z1, P, wrapCount[0]);
		BigInteger U2 = modMulTrack(x2, Z1Z1, P, wrapCount[1]);
		BigInteger S2 = modMulTrack(y2, modMulTrack(Z1, Z1Z1, P, wrapCount[2]), P, wrapCount[3]);
		BigInteger H = modSubTrack(U2, X1, P, wrapCount[4]);
		BigInteger r = modSubTrack(S2, Y1, P, wrapCount[5]);

		if (H.signum() == 0) {
		if (r.signum() == 0) {
		// P == Q
		return pointDouble(X1, Y1, Z1);
		} else {
		// Point at infinity
		return new BigInteger[]{BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO};
		}
		}

		BigInteger H2 = modSqrTrack(H, P, wrapCount[6]);
		BigInteger H3 = modMulTrack(H, H2, P, wrapCount[7]);
		BigInteger V = modMulTrack(X1, H2, P, wrapCount[8]);

		BigInteger rSqr = modSqrTrack(r, P, wrapCount[9]);
		BigInteger twoV = modMulTrack(BigInteger.TWO, V, P, wrapCount[10]);

		BigInteger X3 = modSubTrack(modSubTrack(rSqr, H3, P, wrapCount[11]), twoV, P, wrapCount[12]);
		BigInteger Y3 = modSubTrack(modMulTrack(r, modSubTrack(V, X3, P, wrapCount[13]), P, wrapCount[14]),
		modMulTrack(Y1, H3, P, wrapCount[15]), P, wrapCount[15]);
		BigInteger Z3 = modMulTrack(Z1, H, P, wrapCount[16]);

		return new BigInteger[]{X3, Y3, Z3};
	}

    // Convert Jacobian point to affine point
    public static BigInteger[] toAffinePoint(BigInteger[] jacPoint) {
        return toAffine(jacPoint[0], jacPoint[1], jacPoint[2]);
    }

    // Convert Jacobian point to affine coordinates
    public static BigInteger[] toAffine(BigInteger X, BigInteger Y, BigInteger Z) {
        if (Z.equals(BigInteger.ZERO)) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ZERO}; // Point at infinity
        }
        BigInteger Zinv = Z.modInverse(P);
        BigInteger Zinv2 = modSqr(Zinv);
        BigInteger Zinv3 = modMul(Zinv2, Zinv);
        BigInteger x = mod(modMul(X, Zinv2));
        BigInteger y = mod(modMul(Y, Zinv3));
        return new BigInteger[]{x, y};
    }

    // Print point in affine form
    public static void printPoint(BigInteger[] point) {
        System.out.println("x = " + point[0].toString(16));
        System.out.println("y = " + point[1].toString(16));
    }

    // Print point in affine form
    public static void printPointJac(BigInteger[] point) {
        System.out.println("X = " + point[0].toString(16));
        System.out.println("Y = " + point[1].toString(16));
        System.out.println("Z = " + point[2].toString(16));
    }

    // Print point in affine form with +G and -G
    public static BigInteger[] plusGAffine(BigInteger[] point) {
    	// Generator point in Jacobian form
        BigInteger[] gJacobian = new BigInteger[]{GX, GY, BigInteger.ONE};
    	BigInteger[] pointPlusG = pointAddMixed(point[0], point[1], BigInteger.ONE,
                gJacobian[0], gJacobian[1]);
       BigInteger[] pointPlusGAffine = toAffine(pointPlusG[0],
    		   pointPlusG[1], pointPlusG[2]);
       return pointPlusGAffine;
    }

    public static BigInteger[] minusGAffine(BigInteger[] point) {
    	// Generator point in Jacobian form
        BigInteger[] gJacobian = new BigInteger[]{GX, GY, BigInteger.ONE};
        BigInteger[] negGJacobian = pointNegate(GX, GY, BigInteger.ONE, P);
    	BigInteger[] pointMinusG = pointAddMixed(point[0], point[1], BigInteger.ONE,
    			negGJacobian[0], negGJacobian[1]);
        BigInteger[] pointMinusGAffine = toAffine(pointMinusG[0],
        		pointMinusG[1], pointMinusG[2]);
       return pointMinusGAffine;
    }

    // Print point in affine form with +G and -G
    public static void printPointRegion(BigInteger[] point) {
    	// Generator point in Jacobian form
        BigInteger[] gJacobian = new BigInteger[]{GX, GY, BigInteger.ONE};
        BigInteger[] negGJacobian = pointNegate(GX, GY, BigInteger.ONE, P);
    	BigInteger[] pointMinusG = pointAddMixed(point[0], point[1], BigInteger.ONE,
    			negGJacobian[0], negGJacobian[1]);
        BigInteger[] pointMinusGAffine = toAffine(pointMinusG[0],
        		pointMinusG[1], pointMinusG[2]);
    	BigInteger[] pointPlusG = pointAddMixed(point[0], point[1], BigInteger.ONE,
                gJacobian[0], gJacobian[1]);
       BigInteger[] pointPlusGAffine = toAffine(pointPlusG[0],
    		   pointPlusG[1], pointPlusG[2]);

        System.out.println("Point minus G: " + pointToString(pointMinusGAffine));
        System.out.println("Point itself : " + pointToString(point));
        System.out.println("Point plus G: " + pointToString(pointPlusGAffine));
    }

    public static String pointToString(BigInteger[] point) {
        return "(" + point[0].toString(16) + "," + point[1].toString(16) + ")";
    }

    public static void printPointExpanded(BigInteger x, BigInteger y) {
        System.out.println("x = " + x.toString(16));
        System.out.println("y = " + y.toString(16));
    }

 // Non-constant-time scalar multiplication: k * G
    public static BigInteger[] scalarMultiply(BigInteger k, BigInteger x, BigInteger y) {
        BigInteger[] result = null; // Null means "point at infinity"
        BigInteger[] base = new BigInteger[]{x, y, BigInteger.ONE};

        int length = k.bitLength();

        for (int i = length - 1; i >= 0; i--) {
            if (result != null) {
                result = pointDouble(result[0], result[1], result[2]); // Always double
            }

            if (k.testBit(i)) {
                if (result == null) {
                    // First set bit: initialize result
                    result = new BigInteger[]{base[0], base[1], base[2]};
                } else {
                    // Add base to result
                    result = pointAddMixed(result[0], result[1], result[2], base[0], base[1]);
                }
            }
        }

        // If still null, the result is point at infinity
        if (result == null) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ZERO};
        }

        return toAffine(result[0], result[1], result[2]);
    }

    public static BigInteger[] scalarDivide(BigInteger k, BigInteger x, BigInteger y) {
        // Compute modular inverse of k modulo n
        BigInteger kInv = k.modInverse(CURVE_ORDER);
        // Multiply point by modular inverse scalar
        return scalarMultiply(kInv, x, y);
    }

    public static BigInteger[] pointNegate(BigInteger X, BigInteger Y, BigInteger Z, BigInteger P) {
        return new BigInteger[]{X, P.subtract(Y).mod(P), Z};
    }

    // Negate a Jacobian point, returns a Jacobian point
    public static BigInteger[] pointNegate(BigInteger X, BigInteger Y, BigInteger Z) {
        return new BigInteger[]{X, P.subtract(Y).mod(P), Z};
    }

    public static byte[] compressPoint(BigInteger x, BigInteger y) {
        byte[] xBytes = x.toByteArray();
        // Ensure xBytes is exactly 32 bytes
        if (xBytes.length > 32) {
            xBytes = java.util.Arrays.copyOfRange(xBytes, xBytes.length - 32, xBytes.length);
        } else if (xBytes.length < 32) {
            byte[] tmp = new byte[32];
            System.arraycopy(xBytes, 0, tmp, 32 - xBytes.length, xBytes.length);
            xBytes = tmp;
        }
        byte prefix = y.testBit(0) ? (byte) 0x03 : (byte) 0x02;
        byte[] compressed = new byte[33];
        compressed[0] = prefix;
        System.arraycopy(xBytes, 0, compressed, 1, 32);
        return compressed;
    }

    public static BigInteger modSqrt(BigInteger a, BigInteger p) {
        if (a.signum() == 0) return BigInteger.ZERO;
        if (p.testBit(0) && p.testBit(1)) {
            // p % 4 == 3 shortcut
            BigInteger sqrt = a.modPow(p.shiftRight(2).add(BigInteger.ONE), p);
            if (sqrt.modPow(BigInteger.TWO, p).compareTo(a.mod(p)) == 0) {
                return sqrt;
            } else {
                return null; // No sqrt exists
            }
        }
        // For other primes, full Tonelli-Shanks would be needed (omitted here for brevity)
        throw new UnsupportedOperationException("Tonelli-Shanks not implemented for this prime");
    }

    public static BigInteger modSqrtScalar(BigInteger k, BigInteger modulus) {
        if (k.signum() == 0) return BigInteger.ZERO;

        // Check Legendre symbol (optional, for efficiency)
        // Only compute sqrt if k is a quadratic residue mod modulus
        BigInteger legendre = k.modPow(modulus.subtract(BigInteger.ONE).divide(BigInteger.TWO), modulus);
        if (!legendre.equals(BigInteger.ONE)) {
            return null; // No square root exists
        }

        // Since modulus ≡ 3 mod 4, use the fast method:
        BigInteger exp = modulus.add(BigInteger.ONE).shiftRight(2);
        BigInteger root = k.modPow(exp, modulus);

        // Check that root^2 ≡ k mod modulus (to be safe)
        if (root.multiply(root).mod(modulus).compareTo(k.mod(modulus)) != 0) {
            return null; // Shouldn't happen unless implementation error
        }

        return root;
    }


    public static BigInteger[] decompressPoint(byte[] compressed) throws IllegalArgumentException {
        if (compressed.length != 33) {
            throw new IllegalArgumentException("Invalid compressed point length");
        }
        byte prefix = compressed[0];
        if (prefix != 0x02 && prefix != 0x03) {
            throw new IllegalArgumentException("Invalid compressed point prefix");
        }
        byte[] xBytes = new byte[32];
        System.arraycopy(compressed, 1, xBytes, 0, 32);
        BigInteger x = new BigInteger(1, xBytes);

        // Compute y^2 = x^3 + 7 mod p
        BigInteger y2 = mod(x.modPow(BigInteger.valueOf(3), P).add(BigInteger.valueOf(7)));

        // Compute modular sqrt of y2 mod p using Tonelli-Shanks or similar
        BigInteger y = modSqrt(y2, P);

        if (y == null) {
            throw new IllegalArgumentException("Invalid point compression: no square root found");
        }

        // Use prefix to determine correct y parity
        boolean yIsOdd = y.testBit(0);
        if ((prefix == 0x03) != yIsOdd) {
            y = P.subtract(y); // Choose the other root
        }

        return new BigInteger[] { x, y };
    }

    public static BigInteger[][] precomputeMultiples(BigInteger Gx,
    		BigInteger Gy, int size) {
        BigInteger[][] table = new BigInteger[size][2]; // Each entry is affine point {x, y}

        // Start with 1*G
        table[0] = new BigInteger[]{Gx, Gy};
        lowBitPrecompute.put(pointToKey(table[0]), BigInteger.valueOf(1));


        // Compute each subsequent multiple by adding G
        for (int i = 1; i < size; i++) {
            // Convert previous point to Jacobian coords (X, Y, Z=1)
            BigInteger[] prevJac = new BigInteger[]{table[i-1][0], table[i-1][1], BigInteger.ONE};

            // Add base point G in affine coords to previous
            BigInteger[] sumJac = pointAddMixed(prevJac[0], prevJac[1], prevJac[2], Gx, Gy);

            // Convert back to affine coords
            BigInteger[] sumAffine = toAffine(sumJac[0], sumJac[1], sumJac[2]);

            table[i] = sumAffine;

            lowBitPrecompute.put(pointToKey(sumAffine), BigInteger.valueOf(i + 1));
        }

        return table;
    }

    public static String pointToKey(BigInteger[] p) {
        byte[] compressed = compressPoint(p[0], p[1]);
        StringBuilder sb = new StringBuilder();
        for (byte b : compressed) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString(); // Proper hex string key
    }

    public static void testPointCompression() {
        System.out.println("\n--- testPointCompression ---");

        BigInteger k = new BigInteger("1234567890123456789012345678901234567890");
        BigInteger[] kG = scalarMultiply(k, GX, GY);

        byte[] compressed = compressPoint(kG[0], kG[1]);
        //System.out.println("Compressed point: " + javax.xml.bind.DatatypeConverter.printHexBinary(compressed));

        BigInteger[] decompressed = decompressPoint(compressed);

        boolean match = decompressed[0].equals(kG[0]) && decompressed[1].equals(kG[1]);
        System.out.println("Decompression matches original? " + match);

        if (!match) {
            System.out.println("Original x: " + kG[0].toString(16));
            System.out.println("Decompressed x: " + decompressed[0].toString(16));
            System.out.println("Original y: " + kG[1].toString(16));
            System.out.println("Decompressed y: " + decompressed[1].toString(16));
        }
    }

//    public static void testModSqrtScalar() {
//        System.out.println("\n--- testModSqrtScalar ---");
//
//        BigInteger a = new BigInteger("12345678901234567890");
//        BigInteger k = a.multiply(a).mod(CURVE_ORDER); // k = a^2 mod n
//
//        BigInteger sqrt = modSqrtScalar(k, CURVE_ORDER);
//        if (sqrt == null) {
//            System.out.println("No sqrt found, but expected one.");
//            return;
//        }
//
//        boolean valid = sqrt.multiply(sqrt).mod(CURVE_ORDER).equals(k);
//        System.out.println("√k = " + sqrt);
//        System.out.println("sqrt^2 mod n == k? " + valid);
//
//        // Show both roots
//        BigInteger altRoot = CURVE_ORDER.subtract(sqrt);
//        System.out.println("Alternative root: " + altRoot);
//        System.out.println("Is altRoot^2 == k? " + altRoot.multiply(altRoot).mod(CURVE_ORDER).equals(k));
//    }

    public static void testModSqrtScalar() {
        System.out.println("\n--- testModSqrtScalar ---");

        BigInteger a = new BigInteger("12345678901234567890");
        BigInteger k = a.multiply(a).mod(P); // k = a^2 mod p

        BigInteger sqrt = modSqrtScalar(k, P);
        if (sqrt == null) {
            System.out.println("No sqrt found, but expected one.");
            return;
        }

        boolean valid = sqrt.multiply(sqrt).mod(P).equals(k);
        System.out.println("√k = " + sqrt);
        System.out.println("sqrt^2 mod p == k? " + valid);

        BigInteger altRoot = P.subtract(sqrt);
        System.out.println("Alternative root: " + altRoot);
        System.out.println("Is altRoot^2 == k? " + altRoot.multiply(altRoot).mod(P).equals(k));
    }

 // Curve parameters (example placeholders)
    static BigInteger N;    // Curve order
    static BigInteger[] G;       // Generator point

    // Precompute tables
    static HashMap<String, BigInteger> squarePrecompute = new HashMap<>();
    static HashMap<String, BigInteger> cubePrecompute = new HashMap<>();

    // Generate square and cube precompute regions for scalars < maxRange
    public static void generateSquareCubePrecompute(int maxRange) {
        for (int k = 1; k < maxRange; k++) {
            BigInteger scalar = BigInteger.valueOf(k);

            // Compute square and cube scalars modulo curve order
            BigInteger squareScalar = scalar.multiply(scalar).mod(N);   // k^2 mod n
            BigInteger cubeScalar = squareScalar.multiply(scalar).mod(N); // k^3 mod n

            // Compute EC points for k^2*G and k^3*G
            BigInteger[] squarePoint = scalarMultiply(squareScalar, GX, GY);
            BigInteger[] cubePoint = scalarMultiply(cubeScalar, GX, GY);

            // Store compressed keys and corresponding k
            if (SQUARES_AND_CUBES) {
            	squarePrecompute.put(pointToKey(squarePoint), scalar.pow(2));
            	cubePrecompute.put(pointToKey(cubePoint), scalar.pow(3));
            }
        }
        System.out.println("Square and cube precompute regions generated for range < " + maxRange);
    }


//    public static BigInteger[] pointSubtract(BigInteger[] P, BigInteger[] Q, BigInteger p) {
//        BigInteger[] negQ = pointNegate(Q[0], Q[1], Q[2], p);
//        return pointAddMixed(P, negQ, p); // assuming you have pointAdd implemented in Jacobian
//    }

    // Base point G (secp256k1 generator)
    static final BigInteger GX = new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
    static final BigInteger GY = new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);

	private static final boolean VERBOSE = false;

	private static final boolean CHECK_INTERMEDIATE_FOR_SUB_AND_ADD = false;

	private static final boolean DEBUG_K1_FOR_TESTING = true;

	private static final boolean USE_POINT_DOUBLED_Z_HEURISTIC = false;

    static class TreeNode {
        BigInteger x;          // Affine [x, y]
        BigInteger y;
        TreeNode left;
        TreeNode right;
        int depth;                   // Depth in the recursion tree
        String branchPattern;        // E.g., "L-R-L" (or could use a list)

        BigInteger k1;  // k1 for debugging this

        // Constructor from affine point with optional metadata
        public TreeNode(BigInteger x, BigInteger y, int depth, String branchPattern) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.branchPattern = branchPattern;
        }
    }

    public static boolean checkNode(TreeNode node, String label,
    		boolean isIntermediate) {
        //System.out.println("Point:");
        //printPointExpanded(node.x, node.y);

        String compressedPoint = pointToKey(new BigInteger[] {node.x, node.y});

        if (VERBOSE) {
        System.out.println(label + ": compressedPoint = " + compressedPoint);
        }
        if (squarePrecompute.containsKey(compressedPoint)) {
            BigInteger k1 = squarePrecompute.get(compressedPoint);
            System.out.println("Found scalar in squarePrecompute with value: " +
            		squarePrecompute.get(compressedPoint));
            System.out.println("branch pattern = " + node.branchPattern);
            BigInteger recoveredScalar = recoverScalar(k1, node.branchPattern, isIntermediate);

            System.out.println("Recovered k: " + recoveredScalar);
        	throw new SuccessException("found", recoveredScalar);
        } else {
        	if (VERBOSE) {
            System.out.println("Scalar not found in squarePrecompute");
            System.out.println("square precompute size: " +
            		squarePrecompute.size());

        	}
        }

        if (cubePrecompute.containsKey(compressedPoint)) {
            BigInteger k1 = cubePrecompute.get(compressedPoint);
            System.out.println("Found scalar in cubePrecompute with value: " +
            		cubePrecompute.get(compressedPoint));
            System.out.println("branch pattern = " + node.branchPattern);
            BigInteger recoveredScalar = recoverScalar(k1, node.branchPattern, isIntermediate);

            System.out.println("Recovered k: " + recoveredScalar);
        	throw new SuccessException("found", recoveredScalar);
        } else {
        	if (VERBOSE) {
            System.out.println("Scalar not found in cubePrecompute");
            System.out.println("cube precompute size: " +
            		cubePrecompute.size());
        	}
        }

        if (lowBitPrecompute.containsKey(compressedPoint)) {
            BigInteger k1 = lowBitPrecompute.get(compressedPoint);
			System.out.println("Found scalar in lowBitPrecompute with value: " +
            		k1);
            System.out.println("branch pattern = " + node.branchPattern);
            BigInteger recoveredScalar = recoverScalar(k1, node.branchPattern, isIntermediate);
			System.out.println("Recovered k: " +
            		recoveredScalar);
            	throw new SuccessException("found", recoveredScalar);
        } else {
        	if (VERBOSE) {
            System.out.println("Scalar not found in lowBitPrecompute");
            System.out.println("lowBitPrecompute precompute size: " +
            		lowBitPrecompute.size());
        	}
        }
        return false;

    }

    // Helper: scalarDivide and wrap into TreeNode
    public static TreeNode scalarDivideToNode(BigInteger divisor, BigInteger[] point) {
        BigInteger[] result = scalarDivide(divisor, point[0], point[1]);
        BigInteger recoveredScalar = null;

        // Try to recover scalar from precomputed maps (if using precomputation)
        String key = pointToKey(result);
        if (squarePrecompute.containsKey(key)) {
            recoveredScalar = squarePrecompute.get(key);
        }
        final BigInteger recoveredScalarFinal = recoveredScalar;
        // Fallback: unknown scalar
        return null; //new TreeNode(BigInteger.ZERO) {{
        //    this.point = result;
        //    this.scalar = recoveredScalarFinal != null ? recoveredScalarFinal : BigInteger.ZERO;
        //}};
    }

    // Method to parse the branch pattern and multiply BigInteger accordingly
//    public static BigInteger recoverScalar(BigInteger k1, String branchPattern,
//    		boolean isIntermediate) {
//        // Initialize the scalar to k1
//        BigInteger k = k1;
//
//        if (isIntermediate) {
//        	k = k.add(BigInteger.ONE);
//        }
//
//        // Iterate over each character in the branch pattern
//        for (char ch : branchPattern.toCharArray()) {
//            if (ch == 'R') {
//                // If the character is 'R', we need to multiply by 2
//                k = k.multiply(BigInteger.valueOf(2));
//            } else if (ch == 'L') {
//                // If the character is 'R', we need to multiply by 2
//                k = k.multiply(BigInteger.valueOf(2));
//                k = k.add(BigInteger.ONE);
//            }
//            // Otherwise, if the character is not '-' (it's assumed to be 'R' in your pattern), we do nothing
//        }
//
//        return k;
//    }

    public static BigInteger recoverScalar(BigInteger k1, String branchPattern, boolean isIntermediate) {
        BigInteger k = k1;

        if (isIntermediate) {
            k = k.add(BigInteger.ONE);
        }

        // Walk the pattern in reverse
        for (int i = branchPattern.length() - 1; i >= 0; i--) {
            char ch = branchPattern.charAt(i);
            if (ch == 'R') {
                k = k.multiply(BigInteger.TWO);
            } else if (ch == 'L') {
                k = k.multiply(BigInteger.TWO).add(BigInteger.ONE);
            }
            // Skip '-' or other characters
        }

        return k;
    }

    public static BigInteger recoverScalar2(BigInteger k1, String branchPattern, boolean isIntermediate) {
        BigInteger k = k1;

        if (isIntermediate) {
            k = k.subtract(BigInteger.ONE);
        }

        // Walk the pattern in reverse
        for (int i = branchPattern.length() - 1; i >= 0; i--) {
            char ch = branchPattern.charAt(i);
            if (ch == 'R') {
                k = k.multiply(BigInteger.TWO);
            } else if (ch == 'L') {
                k = k.multiply(BigInteger.TWO).subtract(BigInteger.ONE);
            }
            // Skip '-' or other characters
        }

        return k;
    }

    public static TreeNode scalarDivideToNodeExpanded(BigInteger divisor, BigInteger x,
    		BigInteger y) {
        BigInteger[] result = scalarDivide(divisor, x, y);
        BigInteger recoveredScalar = null;

        // Try to recover scalar from precomputed maps (if using precomputation)
        String key = pointToKey(result);
        if (squarePrecompute.containsKey(key)) {
            recoveredScalar = squarePrecompute.get(key);
        }
        final BigInteger recoveredScalarFinal = recoveredScalar;
        // Fallback: unknown scalar
        return new TreeNode(result[0], result[1], 1, ""); //new TreeNode(BigInteger.ZERO) {{
        //    this.point = result;
        //    this.scalar = recoveredScalarFinal != null ? recoveredScalarFinal : BigInteger.ZERO;
        //}};
    }

    static class SuccessException extends RuntimeException {
    	public BigInteger getK() {
			return k;
		}

		private BigInteger k;

        public SuccessException(String message, BigInteger k) {
            super(message);
            this.k = k;
        }
    }

    static class MutableBoolean {
    	public boolean flag;
    }

    // Method to check if BigInteger is even
    public static boolean isEven(BigInteger n) {
        return n.mod(BigInteger.TWO).equals(BigInteger.ZERO);
    }

    public static boolean buildAndCheckTree(TreeNode node) {
//        if (depth == 0) {
//            checkNode(node, "Leaf Node");
//            return;
//        }
        if (node.depth >= MAX_DEPTH) {
        	return false;
        }
        checkNode(node, "Original", false);

        boolean shouldGoLeft = false;
        boolean even = node.k1.mod(BigInteger.TWO).equals(BigInteger.ZERO);
    	if (even) {
    	      System.out.println("Node k1 is even so we should go right to approach G , k1 is currently " + node.k1);
	  } else {
		  shouldGoLeft = true;
	      System.out.println("Node k1 is odd so we should go left to approach G  , k1 is currently " + node.k1);
	 }

        // Compute (k - 1)G
        BigInteger[] negGJacobian = pointNegate(GX, GY, BigInteger.ONE);
        BigInteger[] nodeJacobian = new BigInteger[]{node.x, node.y, BigInteger.ONE};
        BigInteger[] subtract1Jac = pointAddMixed(nodeJacobian[0], nodeJacobian[1], nodeJacobian[2],
                                                  negGJacobian[0], negGJacobian[1]);
        BigInteger[] subtract1Affine = toAffine(subtract1Jac[0], subtract1Jac[1], subtract1Jac[2]);

        TreeNode preLeftChild = new TreeNode(subtract1Affine[0], subtract1Affine[1], 1, "");
        preLeftChild.branchPattern = node.branchPattern;
        if (CHECK_INTERMEDIATE_FOR_SUB_AND_ADD) {
        	checkNode(preLeftChild, "After (k-1)G", true);
        }

        // Divide (k-1)G by 2
        TreeNode leftChild = scalarDivideToNodeExpanded(TWO, subtract1Affine[0], subtract1Affine[1]);
        leftChild.depth = node.depth + 1;
        leftChild.branchPattern = node.branchPattern + "-L";
        if (node.k1 != null) {
        	leftChild.k1 = node.k1.subtract(BigInteger.ONE);
        	leftChild.k1 = leftChild.k1.divide(BigInteger.TWO);
        } else {
//        	if (leftChild.k1.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
//                //System.out.println("Even");
//            } else {
//                System.out.println("Left child k1 is odd after sub but about to divide by 2, so wrong path");
//            }
        	leftChild.k1 = leftChild.k1.divide(BigInteger.TWO);
        }
        checkNode(leftChild, "After (k-1)G / 2", false);

        // Divide kG by 2 (right child)
        TreeNode rightChild = scalarDivideToNodeExpanded(TWO, node.x, node.y);
        rightChild.depth = node.depth + 1;
        rightChild.branchPattern = node.branchPattern + "-R";
        if (node.k1 != null) {
//        	if (node.k1.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
//                //System.out.println("Even");
//            } else {
//                System.out.println("Node k1 is odd but about to divide by 2, so wrong path");
//            }
        	rightChild.k1 = node.k1.divide(BigInteger.TWO);
        }
        checkNode(rightChild, "After kG / 2", false);

        System.out.println("Left child Doubled Jac: ");
        BigInteger[] pointDoubleLeftJac = pointDoubleJac(leftChild.x, leftChild.y, BigInteger.ONE);
		printPointJac(pointDoubleLeftJac);
        System.out.println("Right child Doubled Jac: ");
        BigInteger[] pointDoubleRightJac = pointDoubleJac(rightChild.x, rightChild.y, BigInteger.ONE);
		printPointJac(pointDoubleRightJac);

        System.out.println("Negative Right child Doubled Jac: ");
        BigInteger[] negativeRight = pointNegate(rightChild.x, rightChild.y, BigInteger.ONE);
        BigInteger[] pointDoubleNegativeRightJac = pointDoubleJac(negativeRight[0],
        		negativeRight[1], negativeRight[2]);
		printPointJac(pointDoubleNegativeRightJac);

        System.out.println("Original point affine: ");
		printPoint(new BigInteger[] {node.x, node.y});

        System.out.println("Original point affine minus one: ");
		printPoint(minusGAffine(new BigInteger[] {node.x, node.y}));

        BigInteger[] pointDoubleLeftAffine = toAffine(pointDoubleLeftJac[0], pointDoubleLeftJac[1], pointDoubleLeftJac[2]);
        System.out.println("Left child doubled affine: ");
		printPoint(pointDoubleLeftAffine);

		// apparently negative left child double affine also reaches the number

        BigInteger[] pointDoubleRightAffine= toAffine(pointDoubleRightJac[0], pointDoubleRightJac[1], pointDoubleRightJac[2]);
        System.out.println("Right child doubled affine: ");
		printPoint(pointDoubleRightAffine);

        BigInteger[] pointDoubleNegativeRightAffine = toAffine(pointDoubleNegativeRightJac[0],
        		pointDoubleNegativeRightJac[1], pointDoubleNegativeRightJac[2]);
        System.out.println("Negative Right child doubled affine: ");
		printPoint(pointDoubleNegativeRightAffine);

        BigInteger[] leftPointJac = new BigInteger[]{leftChild.x, leftChild.y, BigInteger.ONE};
        BigInteger[] gJacobian = new BigInteger[]{GX, GY, BigInteger.ONE};  // Generator point in Jacobian form
        BigInteger[] leftWithGJac = pointAddMixed(leftPointJac[0], leftPointJac[1], leftPointJac[2],
                                                  gJacobian[0], gJacobian[1]);
        BigInteger[] leftWithGAffine = toAffine(leftWithGJac[0], leftWithGJac[1], leftWithGJac[2]);
        System.out.println("Left plus G Affine: " + pointToString(leftWithGAffine));

        BigInteger[] rightPointJac = new BigInteger[]{rightChild.x, rightChild.y, BigInteger.ONE};
        BigInteger[] rightWithGJac = pointAddMixed(rightPointJac[0], rightPointJac[1], rightPointJac[2],
                                                  gJacobian[0], gJacobian[1]);
        BigInteger[] rightWithGAffine = toAffine(rightWithGJac[0], rightWithGJac[1], rightWithGJac[2]);
        System.out.println("Right plus G Affine: " + pointToString(rightWithGAffine));

        BigInteger[] negRightJac = pointNegate(rightChild.x, rightChild.y, BigInteger.ONE);
        BigInteger[] negRightAffine = toAffine(negRightJac[0], negRightJac[1], negRightJac[2]);
        BigInteger[] reflectedRightAffine = negRightAffine;

        BigInteger[] diffJac = pointAddMixed(leftPointJac[0], leftPointJac[1], leftPointJac[2],
        		negRightAffine[0], negRightAffine[1]);
        BigInteger[] diffAffine = toAffine(diffJac[0], diffJac[1], diffJac[2]);

        //BigInteger[] leftPointJacPlusOne = pointAddMixed(leftPointJac[0], leftPointJac[1], leftPointJac[2], GX, GY);
       // BigInteger[] gJacobian = new BigInteger[]{GX, GY, BigInteger.ONE};  // Generator point in Jacobian form

        System.out.println("Difference (left - right) point: x = " +
        		diffAffine[0].toString(16) + ", y = "
        		+ diffAffine[1].toString(16));

        BigInteger[] negLeftJac = pointNegate(leftChild.x, leftChild.y, BigInteger.ONE, P);
        BigInteger[] negLeftAffine = toAffine(negLeftJac[0], negLeftJac[1], negLeftJac[2]);
        BigInteger[] reflectedLeftAffine = negLeftAffine;

        BigInteger[] diffJacRL = pointAddMixed(negLeftJac[0], negLeftJac[1], negLeftJac[2],
        		rightChild.x, rightChild.y);
        BigInteger[] diffAffineRL = toAffine(diffJacRL[0], diffJacRL[1], diffJacRL[2]);

        System.out.println("Difference (right - left) point: x = " +
        		diffAffineRL[0].toString(16) + ", y = "
        		+ diffAffineRL[1].toString(16));

        BigInteger midpointScalar = CURVE_ORDER.add(BigInteger.ONE).divide(TWO);  // N is the order of the curve, P is the prime order

        // Compute the midpoint point (N + 1) / 2 * G
        BigInteger[] midpointPoint = scalarMultiply(midpointScalar, GX, GY);
        System.out.println("Midpoint point: x = " +
        		midpointPoint[0].toString(16) + ", y = "
        		+ midpointPoint[1].toString(16));

        // Compute the torsion point (N -1) / 2 * G
        BigInteger torsionScalar = CURVE_ORDER.subtract(BigInteger.ONE).divide(TWO);
        BigInteger[] torsionPoint = scalarMultiply(torsionScalar, GX, GY);
        System.out.println("Torsion point: x = " +
        		torsionPoint[0].toString(16) + ", y = "
        		+ torsionPoint[1].toString(16));

        BigInteger[] rightUpJac = pointAddMixed(rightChild.x, rightChild.y, BigInteger.ONE,
        		torsionPoint[0], torsionPoint[1]);

        BigInteger[] rightUpAffine = toAffine(rightUpJac[0], rightUpJac[1], rightUpJac[2]);

        BigInteger[] negMidJac = pointNegate(midpointPoint[0],
        		midpointPoint[1], BigInteger.ONE, P);
        System.out.println("negMidJac: x = " +
        		negMidJac[0].toString(16) + ", y = "
        		+ negMidJac[1].toString(16));

        Point leftPoint = new Point(leftChild.x, leftChild.y);
        Point rightPoint = new Point(rightChild.x, rightChild.y);
        Point gPoint = new Point(GX, GY);

        System.out.println("Analysing left");
        Residue residueLeft = new Residue();
        add(leftPoint, gPoint, residueLeft);
        System.out.println("Analysing right");
        Residue residueRight = new Residue();
        add(rightPoint, gPoint, residueRight);

    	MutableCount[] leftCount = new MutableCount[15];
    	for (int i=0;i<15;i++) {
    		leftCount[i] = new MutableCount();
    	}
        pointDoubleWithWrapCount(leftChild.x, leftChild.y, BigInteger.ONE, leftCount);
    	for (int i=0;i<15;i++) {
    		System.out.println("Left count: " + i + " = " + leftCount[i].count.toString(16));
      		//System.out.println("Left count wraps (n/p): " + i + " = " +
      		//		leftCount[i].count.divide(P));
      		//System.out.println("Left count wrap parity = ?");
    	}

    	MutableCount[] rightCount = new MutableCount[15];
    	for (int i=0;i<15;i++) {
    		rightCount[i] = new MutableCount();
    	}
        pointDoubleWithWrapCount(rightChild.x, rightChild.y, BigInteger.ONE, rightCount);
    	for (int i=0;i<15;i++) {
    		System.out.println("Right count: " + i + " = " + rightCount[i].count.toString(16));
      		//System.out.println("Right count wraps (n/p): " + i + " = " +
      			//	rightCount[i].count.divide(P));
      		//System.out.println("Right count wrap parity = ?");

    	}

        boolean useLeft = false;
        // negation of k is Order N - k obviously, so varies with k
        System.out.println("Left x = " + leftChild.x.toString(16));
        System.out.println("Left y = " + leftChild.y.toString(16));
        System.out.println("Left x (reflected by mirror at midpoint) = "
        		+ reflectedLeftAffine[0].toString(16));
        System.out.println("Left y (reflected by mirror at midpoint) = "
        		+ reflectedLeftAffine[1].toString(16));
        System.out.println("Right x = " + rightChild.x.toString(16));
        System.out.println("Right y = " + rightChild.y.toString(16));
        System.out.println("Right x (reflected by mirror at midpoint) = "
        		+ reflectedRightAffine[0].toString(16));
        System.out.println("Right y (reflected by mirror at midpoint) = "
        		+ reflectedRightAffine[1].toString(16));
        System.out.println("Right up x = " + rightUpAffine[0].toString(16));
        System.out.println("Right up y = " + rightUpAffine[1].toString(16));

        System.out.println("Left Point before mirroring:");
        printPoint(new BigInteger[] {leftChild.x, leftChild.y});
        BigInteger[] leftMirroredToMSystem = toAffinePoint(pointNegate(leftChild.x, leftChild.y,
      		  BigInteger.ONE));

        BigInteger[] rightMirroredToMSystem = toAffinePoint(pointNegate(rightChild.x, rightChild.y,
      		  BigInteger.ONE));

        BigInteger[] leftShiftedToMSystem = toAffinePoint(pointAddMixed(leftChild.x, leftChild.y,
      		  BigInteger.ONE, midpointPoint[0], midpointPoint[1]));

        BigInteger[] rightShiftedToMSystem = toAffinePoint(pointAddMixed(rightChild.x, rightChild.y,
      		  BigInteger.ONE, midpointPoint[0], midpointPoint[1]));

        BigInteger[] leftShiftedToMSystemModified = toAffinePoint(pointAddMixed(leftChild.x, leftChild.y,
        		  BigInteger.ONE, torsionPoint[0], torsionPoint[1]));

          BigInteger[] rightShiftedToMSystemModified = toAffinePoint(pointAddMixed(rightChild.x, rightChild.y,
        		  BigInteger.ONE, torsionPoint[0], torsionPoint[1]));

        System.out.println("N/2 Point region:");
        printPointRegion(torsionPoint);
        System.out.println("(N+1)/2 Point region:");
        printPointRegion(midpointPoint);

        System.out.println("Left mirrored to M System:");
        printPointRegion(leftMirroredToMSystem);
        System.out.println("Right mirrored to M System:");
        printPointRegion(rightMirroredToMSystem);
        System.out.println("Left shifted to M System:");
        printPointRegion(leftShiftedToMSystem);
        System.out.println("Right shifted to M System:");
        printPointRegion(rightShiftedToMSystem);
        System.out.println("Left shifted to M System modified:");
        printPointRegion(leftShiftedToMSystemModified);
        System.out.println("Right shifted to M System modified:");
        printPointRegion(rightShiftedToMSystemModified);

        boolean useRight = false;

        if (USE_POINT_DOUBLED_Z_HEURISTIC) {
	    	if (pointDoubleLeftJac[2].compareTo(pointDoubleRightJac[2]) < 0) {
	        	System.out.println("Going left based on pointDoubled Z is less for left");
	        	useLeft = true;
	        	useRight = false;
	    	} else {
	        	System.out.println("Going right based on pointDoubled Z is less for right");
	        	useLeft = false;
	        	useRight = true;
	    	}
        }

        // yes if right shifted point + G = left mirrored point -G then the left is fractional
        if (leftShiftedToMSystem[0].compareTo(rightMirroredToMSystem[0]) == 0) {
        	BigInteger midPoint = P.divide(BigInteger.TWO);

        	// special case for "388f7b0f632de8140fe337e62a37f3566500a99934c2231b6cb9fd7584b8e672"
        	//if (leftShiftedToMSystem[1].toString(16).equals("388f7b0f632de8140fe337e62a37f3566500a99934c2231b6cb9fd7584b8e672")) {

        		//System.out.println("Going right based on special case y: 388f7b0f632de8140fe337e62a37f3566500a99934c2231b6cb9fd7584b8e672");
	        //	useLeft = false;
	        	//useRight = true;
        	//} else if (leftShiftedToMSystem[1].compareTo(midPoint) < 0) {
//        	if (pointDoubleLeftJac[2].compareTo(pointDoubleRightJac[2]) < 0) {
//	        	System.out.println("Going left based on pointDoubled Z is less for left");
//	        	useLeft = true;
//	        	useRight = false;
//        	} else {
//	        	System.out.println("Going right based on pointDoubled Z is less for right");
//	        	useLeft = false;
//	        	useRight = true;
//        	}
        } else {
        	throw new IllegalStateException("Invariant  M System mirror didn't hold");
        	//System.out.println("Going right based on rightShiftedToMSystemModified+G<>leftMirroredToMSystem-G...");
        	//useLeft = false;
        	//useRight = true;
        }


        if (isEven(leftCount[5].count)) {
        	//System.out.println("going right (quotient 5, step E, y flips is even)");
        	//useLeft = false;
        	//useRight = true;
        } else if (!isEven(leftCount[5].count)) {
        	//System.out.println("going left (quotient 5, step E, y flips is even)");
        	//useLeft = true;
        	//useRight = false;
        } else if (leftCount[11].count.compareTo(BigInteger.TWO) == 0) {
        	//System.out.println("going right (special case, quotient 11 is 2)");
        	//useLeft = false;
        	//useRight = true;
        } else if (leftCount[2].count.compareTo(rightCount[2].count) > 0) {
        	//System.out.println("going right (quotient 2 greater on left)");
        	//useLeft = false;
        	//useRight = true;
        } else if (leftCount[2].count.compareTo(rightCount[2].count) < 0) {
        	//System.out.println("going left (quotient 2 greater on right)");
        	//useLeft = true;
        	//useRight = false;
        }  else if (leftCount[5].count.compareTo(rightCount[5].count) > 0) {
        	System.out.println("going right (quotient 5 greater on left)");
        	//useLeft = false;
        	//useRight = true;
        } else if (leftCount[5].count.compareTo(rightCount[5].count) < 0) {
        	System.out.println("going left (quotient 5 greater on right)");
        	//useLeft = true;
        	//useRight = false;
        } else {
        	System.out.println("count 5 not different, so comparing 4");
            if (leftCount[4].count.compareTo(rightCount[4].count) > 0) {
            	System.out.println("going right (quotient 4 greater on left)");
            	//useLeft = false;
            	//useRight = true;
            } else if (leftCount[4].count.compareTo(rightCount[4].count) < 0) {
            	System.out.println("going left (quotient 4 greater on right)");
            	//useLeft = true;
            	//useRight = false;
            } else {
            	throw new IllegalStateException("quotient 4 and 5 the same");
            }
        }

        if (leftChild.y.compareTo(rightChild.y) < 0) {
//        	//left path was correct, so discard right path somehow
//        	useLeft = true;
        	//System.out.println("left.y is less than right.y");
        	//System.out.println("left.y is less than right.y so going right");
        	//useLeft = false;
        	//useRight = true;
        } else if (leftChild.y.compareTo(rightChild.y) > 0) {
        	//System.out.println("left.y is greater than right.y");

//            	//left path was correct, so discard right path somehow
//            	useLeft = true;
            	//System.out.println("left.y is greater than right.y so going left");
            	//useLeft = true;
            	//useRight = false;
        } else {
        	//if (leftChild.y.compareTo(rightChild.y) < 0) {
//            	//left path was correct, so discard right path somehow
//            	useLeft = true;
            throw new IllegalStateException("did not expect left.y is equal to right.y");
            //}
        }

        BigInteger pHalf = P.shiftRight(1);

        BigInteger leftChildYModP = leftChild.y.mod(P);
        BigInteger rightChildYModP = rightChild.y.mod(P);

        System.out.println("left y mod p is:  " + leftChildYModP.toString(16));
        System.out.println("right y mod p is: " + rightChildYModP.toString(16));

        if (leftChildYModP.compareTo(rightChildYModP) < 0) {
        	//System.out.println("left.y mod p is less than right.y mod p, so going right");
        	//System.out.println("left.y is less than right.y so going right");
        	//useLeft = false;
        	//useRight = true;
        } else if (leftChildYModP.compareTo(rightChildYModP) > 0) {
        	//System.out.println("left.y mod p is greater than right.y mod p, so going left");
        	//useLeft = true;
        	//useRight = false;
//            	//left path was correct, so discard right path somehow
//            	useLeft = true;
            	//System.out.println("left.y is greater than right.y so going left");
            	//useLeft = true;
            	//useRight = false;
        } else {
        	//if (leftChild.y.compareTo(rightChild.y) < 0) {
//            	//left path was correct, so discard right path somehow
//            	useLeft = true;
            //throw new IllegalStateException("did not expect left.y mod p is equal to right.y mod p");
            //}
        	System.out.println("left y mod p is equal to right y mod p as expected");
        }

		boolean leftNegated = leftChildYModP.compareTo(pHalf) > 0;
        boolean rightNegated = rightChildYModP.compareTo(pHalf) > 0;


        if (!leftNegated && rightNegated) {
            // Left is unflipped, more likely correct
            //useLeft = true;
            //useRight = false;
           // System.out.println("Left not negated and right is negated, using left (sub and half branch)");
        } else if (leftNegated && !rightNegated) {
            // Right is unflipped, more likely correct
            //useLeft = false;
            //useRight = true;
            //System.out.println("Left negated and right is not negated, using right (half branch)");
        } else if (leftNegated && rightNegated) {
            //throw new IllegalStateException("Left is negated and right is negated, using both (2 branches)");

            // Both are unflipped or both flipped — ambiguity
            // You might recurse on both or pick by other heuristic
        	//useLeft = true;
        	//useRight = true;
        } else if (!leftNegated && !rightNegated) {
            //throw new IllegalStateException("Left not negated and right is not negated, using both (2 branches)");

            // Both are unflipped or both flipped — ambiguity
            // You might recurse on both or pick by other heuristic
        	//useLeft = true;
        	//useRight = true;
        }
        System.out.println("Depth is currently (node param): " + node.depth);
        System.out.println("k1 parent (for testing) = " + node.k1);
        System.out.println("k1 left BigInteger = " + leftChild.k1);
        System.out.println("k1 right BigInteger " + rightChild.k1);

        BigDecimal k4 = new BigDecimal(node.k1.toString());
        System.out.println("k1 left BigDecimal = " +
        		k4.subtract(BigDecimal.ONE).divide(new BigDecimal("2")
        		, MathContext.DECIMAL128));
        System.out.println("k1 right BigDecimal = " +
        		k4.divide(new BigDecimal("2")
        		, MathContext.DECIMAL128));


        // here check if its - ((N+1)/2)G or + ((N+1/2)G

    	//if (pointDoubleLeftJac[2].compareTo(pointDoubleRightJac[2]) < 0) {
        if (pointDoubleLeftAffine[0].compareTo(node.x) == 0) {
        	System.out.println("Going left based on pointDoubled Affine matches for left");
        	useLeft = true;
        	useRight = false;
    	} else if (pointDoubleRightAffine[0].compareTo(node.x) == 0) {
        	System.out.println("Going right based on pointDoubled Affine matches for right");
        	useLeft = false;
        	useRight = true;
    	} else {
    		throw new IllegalStateException("Neither of the points doubled");
    	}

        if (useLeft) {
        	// Left was wrong direction with residue 011101
        	if (residueLeft.U1 == 0 && residueLeft.U2 == 1
        			&& residueLeft.S1 == 1) {
        		System.out.println("Special case residue 011nnn go right");
        		useLeft = false;
        		useRight = true;
        	}

        	// Special case residues H = 0 and R = 0 so go right
        	if (residueLeft.H == 0 && residueLeft.R == 0) {
        		System.out.println("Special case residues H = 0 and R = 0 so go right");
        		useLeft = false;
        		useRight = true;
        	} else {
        		System.out.println("Not a residue special case");
        	}
        }

        if (useRight) {
        	// Special case residues H = 0 and R = 0 so go left
        	if (residueRight.U1 == 1 && residueRight.U2 == 1
        			&& residueRight.S1 == 1 && residueRight.S2 == 1
        			&& residueRight.H == 0 && residueRight.R == 0) {
        		System.out.println("Special case residues: all of U1, U2, S1, S2 = 1 and H, R = 0 so go use previous heuristic");
        		//useLeft = true;
        		//useRight = false;
        	} else if (residueRight.H == 1 && residueRight.R == 1) {
        		System.out.println("Special case residues H = 1 and R = 1 so go left");
        		useLeft = true;
        		useRight = false;
        	} else if (residueRight.H == 0 && residueRight.R == 0) {
        		System.out.println("Special case residues H = 0 and R = 0 so go left");
        		useLeft = true;
        		useRight = false;
        	} else {
        		System.out.println("Not a residue special case");
        	}
        }

        if (useLeft && !shouldGoLeft) {
        	throw new IllegalStateException("Should have gone right");
        } else if (useRight && shouldGoLeft) {
        	throw new IllegalStateException("Should have gone left");
        }

        // Recurse (two flags in case both branches required)
        if (useLeft) {
	        boolean success = buildAndCheckTree(leftChild);
	        if (success) {
	        	throw new SuccessException("found", BigInteger.ZERO);
	        }
        }
        if (useRight) {
	        boolean success = buildAndCheckTree(rightChild);
	        if (success) {
	        	throw new SuccessException("found", BigInteger.ZERO);
	        }
        }
        return false;
    }

    public static boolean buildAndCheckTree2(TreeNode node) {
//      if (depth == 0) {
//          checkNode(node, "Leaf Node");
//          return;
//      }
      if (node.depth >= MAX_DEPTH) {
      	return false;
      }
      checkNode(node, "Original", false);

      boolean even = node.k1.mod(BigInteger.TWO).equals(BigInteger.ZERO);
  	if (even) {
  	      System.out.println("Node k1 is even so we should go right to approach G , k1 is currently " + node.k1);
	} else {
	    System.out.println("Node k1 is odd so we should go left to approach G  , k1 is currently " + node.k1);
	}

      // Compute (k - 1)G
      BigInteger[] gJacobian = new BigInteger[] {GX, GY, BigInteger.ONE};
      BigInteger[] nodeJacobian = new BigInteger[]{node.x, node.y, BigInteger.ONE};
      BigInteger[] add1Jac = pointAddMixed(nodeJacobian[0], nodeJacobian[1], nodeJacobian[2],
    		  gJacobian[0], gJacobian[1]);
      BigInteger[] add1Affine = toAffine(add1Jac[0], add1Jac[1], add1Jac[2]);

      TreeNode preLeftChild = new TreeNode(add1Affine[0], add1Affine[1], 1, "");
      preLeftChild.branchPattern = node.branchPattern;
      if (CHECK_INTERMEDIATE_FOR_SUB_AND_ADD) {
      	checkNode(preLeftChild, "After (k-1)G", true);
      }

      // Divide (k-1)G by 2
      TreeNode leftChild = scalarDivideToNodeExpanded(TWO, add1Affine[0], add1Affine[1]);
      leftChild.depth = node.depth + 1;
      leftChild.branchPattern = node.branchPattern + "-L";
      if (node.k1 != null) {
      	leftChild.k1 = node.k1.subtract(BigInteger.ONE);
      	leftChild.k1 = leftChild.k1.divide(BigInteger.TWO);
      } else {
//      	if (leftChild.k1.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
//              //System.out.println("Even");
//          } else {
//              System.out.println("Left child k1 is odd after sub but about to divide by 2, so wrong path");
//          }
      	leftChild.k1 = leftChild.k1.divide(BigInteger.TWO);
      }
      checkNode(leftChild, "After (k-1)G / 2", false);

      // Divide kG by 2 (right child)
      TreeNode rightChild = scalarDivideToNodeExpanded(TWO, node.x, node.y);
      rightChild.depth = node.depth + 1;
      rightChild.branchPattern = node.branchPattern + "-R";
      if (node.k1 != null) {
//      	if (node.k1.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
//              //System.out.println("Even");
//          } else {
//              System.out.println("Node k1 is odd but about to divide by 2, so wrong path");
//          }
      	rightChild.k1 = node.k1.divide(BigInteger.TWO);
      }
      checkNode(rightChild, "After kG / 2", false);

      System.out.println("Left child Doubled Jac: ");
      printPointJac(pointDoubleJac(leftChild.x, leftChild.y, BigInteger.ONE));
      System.out.println("Right child Doubled Jac: ");
      printPointJac(pointDoubleJac(rightChild.x, rightChild.y, BigInteger.ONE));

      BigInteger[] leftPointJac = new BigInteger[]{leftChild.x, leftChild.y, BigInteger.ONE};
      //BigInteger[] gJacobian = new BigInteger[]{GX, GY, BigInteger.ONE};  // Generator point in Jacobian form
      BigInteger[] leftWithGJac = pointAddMixed(leftPointJac[0], leftPointJac[1], leftPointJac[2],
                                                gJacobian[0], gJacobian[1]);
      BigInteger[] leftWithGAffine = toAffine(leftWithGJac[0], leftWithGJac[1], leftWithGJac[2]);

      BigInteger[] negRightJac = pointNegate(rightChild.x, rightChild.y, BigInteger.ONE, P);
      BigInteger[] negRightAffine = toAffine(negRightJac[0], negRightJac[1], negRightJac[2]);
      BigInteger[] reflectedRightAffine = negRightAffine;

      BigInteger[] diffJac = pointAddMixed(leftPointJac[0], leftPointJac[1], leftPointJac[2],
      		negRightAffine[0], negRightAffine[1]);
      BigInteger[] diffAffine = toAffine(diffJac[0], diffJac[1], diffJac[2]);

      System.out.println("Difference (left - right) point: x = " +
      		diffAffine[0].toString(16) + ", y = "
      		+ diffAffine[1].toString(16));

      BigInteger[] negLeftJac = pointNegate(leftChild.x, leftChild.y, BigInteger.ONE, P);
      BigInteger[] negLeftAffine = toAffine(negLeftJac[0], negLeftJac[1], negLeftJac[2]);
      BigInteger[] reflectedLeftAffine = negLeftAffine;

      BigInteger[] diffJacRL = pointAddMixed(negLeftJac[0], negLeftJac[1], negLeftJac[2],
      		rightChild.x, rightChild.y);
      BigInteger[] diffAffineRL = toAffine(diffJacRL[0], diffJacRL[1], diffJacRL[2]);

      System.out.println("Difference (right - left) point: x = " +
      		diffAffineRL[0].toString(16) + ", y = "
      		+ diffAffineRL[1].toString(16));

      BigInteger midpointScalar = CURVE_ORDER.add(BigInteger.ONE).divide(TWO);  // N is the order of the curve, P is the prime order

      // Compute the midpoint point (N + 1) / 2 * G
      BigInteger[] midpointPoint = scalarMultiply(midpointScalar, GX, GY);
      System.out.println("Midpoint point: x = " +
      		midpointPoint[0].toString(16) + ", y = "
      		+ midpointPoint[1].toString(16));

      // Compute the torsion point (N -1) / 2 * G
      BigInteger torsionScalar = CURVE_ORDER.subtract(BigInteger.ONE).divide(TWO);
      BigInteger[] torsionPoint = scalarMultiply(torsionScalar, GX, GY);
      System.out.println("Torsion point: x = " +
      		torsionPoint[0].toString(16) + ", y = "
      		+ torsionPoint[1].toString(16));

      BigInteger[] rightUpJac = pointAddMixed(rightChild.x, rightChild.y, BigInteger.ONE,
      		torsionPoint[0], torsionPoint[1]);

      BigInteger[] rightUpAffine = toAffine(rightUpJac[0], rightUpJac[1], rightUpJac[2]);

      BigInteger[] leftMirroredToMSystem = pointNegate(leftChild.x, leftChild.y,
    		  P, BigInteger.ONE);

      BigInteger[] rightMirroredToMSystem = pointNegate(leftChild.x, leftChild.y,
    		  P, BigInteger.ONE);

      BigInteger[] leftShiftedToMSystem = pointAddMixed(leftChild.x, leftChild.y,
    		  BigInteger.ONE, midpointPoint[0], midpointPoint[1]);

      BigInteger[] rightShiftedToMSystem = pointAddMixed(leftChild.x, leftChild.y,
    		  BigInteger.ONE, midpointPoint[0], midpointPoint[1]);

      System.out.println("Left mirrored to M System:");
      printPointRegion(leftMirroredToMSystem);
      System.out.println("Right mirrored to M System:");
      printPointRegion(rightMirroredToMSystem);
      System.out.println("Left shifted to M System:");
      printPointRegion(leftShiftedToMSystem);
      System.out.println("Right shifted to M System:");
      printPointRegion(rightShiftedToMSystem);


      BigInteger[] negMidJac = pointNegate(midpointPoint[0],
      		midpointPoint[1], BigInteger.ONE, P);
      System.out.println("negMidJac: x = " +
      		negMidJac[0].toString(16) + ", y = "
      		+ negMidJac[1].toString(16));

  	MutableCount[] leftCount = new MutableCount[15];
  	for (int i=0;i<15;i++) {
  		leftCount[i] = new MutableCount();
  	}
      pointDoubleWithWrapCount(leftChild.x, leftChild.y, BigInteger.ONE, leftCount);
  	for (int i=0;i<15;i++) {
  		System.out.println("Left count: " + i + " = " + leftCount[i].count);
    		//System.out.println("Left count wraps (n/p): " + i + " = " +
    		//		leftCount[i].count.divide(P));
    		//System.out.println("Left count wrap parity = ?");
  	}

  	MutableCount[] rightCount = new MutableCount[15];
  	for (int i=0;i<15;i++) {
  		rightCount[i] = new MutableCount();
  	}
      pointDoubleWithWrapCount(rightChild.x, rightChild.y, BigInteger.ONE, rightCount);
  	for (int i=0;i<15;i++) {
  		System.out.println("Right count: " + i + " = " + rightCount[i].count);
    		//System.out.println("Right count wraps (n/p): " + i + " = " +
    			//	rightCount[i].count.divide(P));
    		//System.out.println("Right count wrap parity = ?");

  	}

      boolean useLeft = false;
      // negation of k is Order N - k obviously, so varies with k
      System.out.println("Left x = " + leftChild.x.toString(16));
      System.out.println("Left y = " + leftChild.y.toString(16));
      System.out.println("Left x (reflected by mirror at midpoint) = "
      		+ reflectedLeftAffine[0].toString(16));
      System.out.println("Left y (reflected by mirror at midpoint) = "
      		+ reflectedLeftAffine[1].toString(16));
      System.out.println("Right x = " + rightChild.x.toString(16));
      System.out.println("Right y = " + rightChild.y.toString(16));
      System.out.println("Right x (reflected by mirror at midpoint) = "
      		+ reflectedRightAffine[0].toString(16));
      System.out.println("Right y (reflected by mirror at midpoint) = "
      		+ reflectedRightAffine[1].toString(16));
      System.out.println("Right up x = " + rightUpAffine[0].toString(16));
      System.out.println("Right up y = " + rightUpAffine[1].toString(16));

      boolean useRight = false;

      if (isEven(leftCount[5].count)) {
      	System.out.println("going right (quotient 5, step E, y flips is even)");
      	useLeft = false;
      	useRight = true;
      } else if (!isEven(leftCount[5].count)) {
      	System.out.println("going left (quotient 5, step E, y flips is even)");
      	useLeft = true;
      	useRight = false;
      } else if (leftCount[11].count.compareTo(BigInteger.TWO) == 0) {
      	System.out.println("going right (special case, quotient 11 is 2)");
      	useLeft = false;
      	useRight = true;
      } else if (leftCount[2].count.compareTo(rightCount[2].count) > 0) {
      	System.out.println("going right (quotient 2 greater on left)");
      	useLeft = false;
      	useRight = true;
      } else if (leftCount[2].count.compareTo(rightCount[2].count) < 0) {
      	System.out.println("going left (quotient 2 greater on right)");
      	useLeft = true;
      	useRight = false;
      }  else if (leftCount[5].count.compareTo(rightCount[5].count) > 0) {
      	System.out.println("going right (quotient 5 greater on left)");
      	useLeft = false;
      	useRight = true;
      } else if (leftCount[5].count.compareTo(rightCount[5].count) < 0) {
      	System.out.println("going left (quotient 5 greater on right)");
      	useLeft = true;
      	useRight = false;
      } else {
      	System.out.println("count 5 not different, so comparing 4");
          if (leftCount[4].count.compareTo(rightCount[4].count) > 0) {
          	System.out.println("going right (quotient 4 greater on left)");
          	useLeft = false;
          	useRight = true;
          } else if (leftCount[4].count.compareTo(rightCount[4].count) < 0) {
          	System.out.println("going left (quotient 4 greater on right)");
          	useLeft = true;
          	useRight = false;
          } else {
          	throw new IllegalStateException("quotient 4 and 5 the same");
          }
      }

      if (leftChild.y.compareTo(rightChild.y) < 0) {
//      	//left path was correct, so discard right path somehow
//      	useLeft = true;
      	//System.out.println("left.y is less than right.y");
      	//System.out.println("left.y is less than right.y so going right");
      	//useLeft = false;
      	//useRight = true;
      } else if (leftChild.y.compareTo(rightChild.y) > 0) {
      	//System.out.println("left.y is greater than right.y");

//          	//left path was correct, so discard right path somehow
//          	useLeft = true;
          	//System.out.println("left.y is greater than right.y so going left");
          	//useLeft = true;
          	//useRight = false;
      } else {
      	//if (leftChild.y.compareTo(rightChild.y) < 0) {
//          	//left path was correct, so discard right path somehow
//          	useLeft = true;
          throw new IllegalStateException("did not expect left.y is equal to right.y");
          //}
      }

      BigInteger pHalf = P.shiftRight(1);

      BigInteger leftChildYModP = leftChild.y.mod(P);
      BigInteger rightChildYModP = rightChild.y.mod(P);

      System.out.println("left y mod p is:  " + leftChildYModP.toString(16));
      System.out.println("right y mod p is: " + rightChildYModP.toString(16));

      if (leftChildYModP.compareTo(rightChildYModP) < 0) {
      	//System.out.println("left.y mod p is less than right.y mod p, so going right");
      	//System.out.println("left.y is less than right.y so going right");
      	//useLeft = false;
      	//useRight = true;
      } else if (leftChildYModP.compareTo(rightChildYModP) > 0) {
      	//System.out.println("left.y mod p is greater than right.y mod p, so going left");
      	//useLeft = true;
      	//useRight = false;
//          	//left path was correct, so discard right path somehow
//          	useLeft = true;
          	//System.out.println("left.y is greater than right.y so going left");
          	//useLeft = true;
          	//useRight = false;
      } else {
      	//if (leftChild.y.compareTo(rightChild.y) < 0) {
//          	//left path was correct, so discard right path somehow
//          	useLeft = true;
          //throw new IllegalStateException("did not expect left.y mod p is equal to right.y mod p");
          //}
      	System.out.println("left y mod p is equal to right y mod p as expected");
      }

		boolean leftNegated = leftChildYModP.compareTo(pHalf) > 0;
      boolean rightNegated = rightChildYModP.compareTo(pHalf) > 0;


      if (!leftNegated && rightNegated) {
          // Left is unflipped, more likely correct
          //useLeft = true;
          //useRight = false;
         // System.out.println("Left not negated and right is negated, using left (sub and half branch)");
      } else if (leftNegated && !rightNegated) {
          // Right is unflipped, more likely correct
          //useLeft = false;
          //useRight = true;
          //System.out.println("Left negated and right is not negated, using right (half branch)");
      } else if (leftNegated && rightNegated) {
          //throw new IllegalStateException("Left is negated and right is negated, using both (2 branches)");

          // Both are unflipped or both flipped — ambiguity
          // You might recurse on both or pick by other heuristic
      	//useLeft = true;
      	//useRight = true;
      } else if (!leftNegated && !rightNegated) {
          //throw new IllegalStateException("Left not negated and right is not negated, using both (2 branches)");

          // Both are unflipped or both flipped — ambiguity
          // You might recurse on both or pick by other heuristic
      	//useLeft = true;
      	//useRight = true;
      }
      System.out.println("Depth is currently (node param): " + node.depth);
      System.out.println("k1 parent (for testing) = " + node.k1);
      System.out.println("k1 left BigInteger = " + leftChild.k1);
      System.out.println("k1 right BigInteger " + rightChild.k1);

      BigDecimal k4 = new BigDecimal(node.k1.toString());
      System.out.println("k1 left BigDecimal = " +
      		k4.subtract(BigDecimal.ONE).divide(new BigDecimal("2")
      		, MathContext.DECIMAL128));
      System.out.println("k1 right BigDecimal = " +
      		k4.divide(new BigDecimal("2")
      		, MathContext.DECIMAL128));


      // here check if its - ((N+1)/2)G or + ((N+1/2)G

      // Recurse
      if (useLeft) {
	        boolean success = buildAndCheckTree2(leftChild);
	        if (success) {
	        	throw new SuccessException("found", BigInteger.ZERO);
	        }
      }
      if (useRight) {
	        boolean success = buildAndCheckTree2(rightChild);
	        if (success) {
	        	throw new SuccessException("found", BigInteger.ZERO);
	        }
      }
      return false;
    }


    // Main for test
    public static void main(String[] args) {
        // G in Jacobian coords = (x, y, 1)
        BigInteger[] G = new BigInteger[]{GX, GY, BigInteger.ONE};

        // Compute 2G = G + G
        BigInteger[] twoG = pointDouble(G[0], G[1], G[2]);
        BigInteger[] twoG_affine = toAffine(twoG[0], twoG[1], twoG[2]);

        System.out.println("2G (Affine):");
        printPoint(twoG_affine);

        // Compute 3G = 2G + G (mixed addition)
        BigInteger[] threeG = pointAddMixed(twoG[0], twoG[1], twoG[2], GX, GY);
        BigInteger[] threeG_affine = toAffine(threeG[0], threeG[1], threeG[2]);

        System.out.println("\n3G (Affine):");
        printPoint(threeG_affine);

        {
        BigInteger k = BigInteger.valueOf(7);
        BigInteger[] kG = scalarMultiply(k, GX, GY);

        System.out.println("\n7G (Affine):");
        printPoint(kG);
        }
        {
        BigInteger k = new BigInteger("756234554646547657545643645646");
        BigInteger[] kG = scalarMultiply(k, GX, GY);

        System.out.println("\n756234554646547657545643645646G (Affine):");
        printPoint(kG);
        }
        System.out.flush();
        test2(args);
        testPointCompression();
        testModSqrtScalar();

        int numLowBits = LOW_BIT_COUNT_FOR_PRECOMPUTE;
        BigInteger[][] precomputedG = precomputeMultiples(GX, GY,
        		(int) (1L << numLowBits));
        System.out.println("Precomputed multiples of G ready, size: "
        		+ precomputedG.length);

        testRecursiveTreeFromRandomScalar();

    }

    public static void test2(String[] args) {
        // secp256k1 base point G coordinates (hex)
        BigInteger Gx = GX;
        BigInteger Gy = GY;

        BigInteger scalar140 = BigInteger.valueOf(140);
        BigInteger scalar2 = TWO;

        // Calculate 140G
        BigInteger[] point140G = scalarMultiply(scalar140, Gx, Gy);

        // Divide 140G by 2 (i.e., multiply by modular inverse of 2)
        BigInteger[] dividedPoint = scalarDivide(scalar2, point140G[0], point140G[1]);

        // Calculate 70G directly for comparison
        BigInteger scalar70 = BigInteger.valueOf(70);
        BigInteger[] point70G = scalarMultiply(scalar70, Gx, Gy);

        // Check if dividedPoint equals point70G
        if (dividedPoint[0].equals(point70G[0]) && dividedPoint[1].equals(point70G[1])) {
            System.out.println("Test PASSED: (140G) / 2 == 70G");
        } else {
            System.out.println("Test FAILED!");
            System.out.println("Divided point: (" + dividedPoint[0].toString(16) + ", " + dividedPoint[1].toString(16) + ")");
            System.out.println("Expected 70G: (" + point70G[0].toString(16) + ", " + point70G[1].toString(16) + ")");
        }
        test3(args);
    }

    public static void test3(String[] args) {
        BigInteger Gx = new BigInteger(
                "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
            BigInteger Gy = new BigInteger(
                "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);


        // Choose known k > 2^192
        BigInteger k = BigInteger.ONE.shiftLeft(192).add(BigInteger.valueOf(123456789)); // example k > 2^192

        System.out.println("Original k: " + k);

        // Compute P = kG
        BigInteger[] P = scalarMultiply(k, Gx, Gy);

        // Divide P by 2^192 using scalarDivide
        BigInteger twoPower192 = BigInteger.ONE.shiftLeft(192);
        BigInteger[] Q = scalarDivide(twoPower192, P[0], P[1]);

        // Now calculate s = k * 2^(-192) mod CURVE_ORDER
        BigInteger kInv = twoPower192.modInverse(CURVE_ORDER);
        BigInteger s = k.multiply(kInv).mod(CURVE_ORDER);

        System.out.println("Computed scalar s (k * 2^-192 mod n): " + s);

        // Verify that Q == sG
        BigInteger[] sG = scalarMultiply(s, Gx, Gy);

        boolean equal = Q[0].equals(sG[0]) && Q[1].equals(sG[1]);

        System.out.println("Q equals sG? " + equal);

        // Verify recomputing k: s * 2^192 mod n == original k mod n
        BigInteger recomputedK = s.multiply(twoPower192).mod(CURVE_ORDER);
        System.out.println("Recomputed k matches original k? " + recomputedK.equals(k.mod(CURVE_ORDER)));
        test4(args);
    }

    public static void test4(String[] args) {
        // secp256k1 base point G coordinates (hex)
        BigInteger Gx = new BigInteger(
            "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
        BigInteger Gy = new BigInteger(
            "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);

        BigInteger scalar140 = BigInteger.valueOf(140);
        BigInteger scalar2 = TWO;

        // Calculate 141G
        BigInteger[] point140G = scalarMultiply(scalar140, Gx, Gy);

        // Divide 141G by 2 (i.e., multiply by modular inverse of 2)
        BigInteger[] dividedPoint = scalarDivide(scalar2, point140G[0], point140G[1]);

        // Calculate 70G directly for comparison
        BigInteger scalar70 = BigInteger.valueOf(70);
        BigInteger[] point70G = scalarMultiply(scalar70, Gx, Gy);

        // Check if dividedPoint equals point70G
        if (dividedPoint[0].equals(point70G[0]) && dividedPoint[1].equals(point70G[1])) {
            System.out.println("Test PASSED: (140G) / 2 == 70G");
        } else {
            System.out.println("Test FAILED!");
            System.out.println("Divided point: (" + dividedPoint[0].toString(16) + ", " + dividedPoint[1].toString(16) + ")");
            System.out.println("Expected 70G: (" + point70G[0].toString(16) + ", " + point70G[1].toString(16) + ")");
        }
        testSquareCubePrecompute(4096*2);
    }

    public static void testSquareCubePrecompute(int maxRange) {
        System.out.println("\n--- testSquareCubePrecompute ---");

        // Initialize global curve parameters
        N = CURVE_ORDER;
        G = new BigInteger[]{GX, GY};

        // Generate precompute tables
        generateSquareCubePrecompute(maxRange);

        boolean allPassed = true;

        for (int k = 1; k < maxRange; k++) {
            BigInteger scalar = BigInteger.valueOf(k);

            // Compute square and cube scalars
            BigInteger squareScalar = scalar.multiply(scalar).mod(N);
            BigInteger cubeScalar = squareScalar.multiply(scalar).mod(N);

            // Compute points
            BigInteger[] squarePoint = scalarMultiply(squareScalar, GX, GY);
            BigInteger[] cubePoint = scalarMultiply(cubeScalar, GX, GY);

            // Compress
            String squareKey = pointToKey(squarePoint);
            String cubeKey = pointToKey(cubePoint);

            // Lookup
            BigInteger recoveredFromSquare = squarePrecompute.get(squareKey);
            BigInteger recoveredFromCube = cubePrecompute.get(cubeKey);

            boolean squareOk = scalar.equals(recoveredFromSquare);
            boolean cubeOk = scalar.equals(recoveredFromCube);

            if (!squareOk || !cubeOk) {
                System.out.println("Test FAILED for k = " + k);
                System.out.println("Expected: " + scalar);
                System.out.println("From squareKey: " + squareKey);
                System.out.println("From cubeKey: " + cubeKey);
                System.out.println("From squarePrecompute: " + recoveredFromSquare);
                System.out.println("From cubePrecompute: " + recoveredFromCube);
                allPassed = false;
                break;
            }
        }

        if (allPassed) {
            System.out.println("All square and cube precompute tests PASSED for range < " + maxRange);
        }
        //precomputeMultiples();
    }

    public static void testRecursiveTreeFromRandomScalar() {
        System.out.println("\n--- testRecursiveTreeFromRandomScalar ---");

        // 256-bit secure random scalar mod curve order
        SecureRandom rand = new SecureRandom();
        BigInteger k;
        do {
            k = new BigInteger(256, rand).mod(CURVE_ORDER);
        } while (k.signum() == 0); // avoid zero scalar

        k = new BigInteger("24");  //21
        System.out.println("Random scalar k (for P = kG) = " + k);
        System.out.println("Performing subtract and half vs. half descent binary tree");
        {
        // Compute kG
        BigInteger[] point = scalarMultiply(k, GX, GY);
        System.out.println("kG:");
        printPoint(point);

        // Create tree node
        TreeNode root = new TreeNode(point[0], point[1], 0, "");
        if (DEBUG_K1_FOR_TESTING) {
        	root.k1 = k;
        }
        //root.point = point; // Ensure consistency

        // Build and check tree with depth 2 (adjustable)
        try {
        	buildAndCheckTree(root);
        } catch (SuccessException e) {
        	System.out.println("SuccessException, k = " + e.getK());
        }

        //k = new BigInteger("2").pow(16).add(new BigInteger("156234987"));
        //System.out.println("Random scalar k = " + k);
        }

//        {
//        // Compute kG
//        BigInteger[] point = scalarMultiply(k, GX, GY);
//        System.out.println("kG:");
//        printPoint(point);
//
//        // Create tree node
//        TreeNode root = new TreeNode(point[0], point[1], 0, "");
//        //root.point = point; // Ensure consistency
//
//        // Build and check tree with depth 2 (adjustable)
//        try {
//        	buildAndCheckTree(root);
//        } catch (SuccessException e) {
//        	System.out.println("SuccessException, k = " + e.getK());
//        }
//        }
    }


    int[] weights = {
    	    1,  // X1^2 (low weight)
    	    3,  // Y1^2 (important)
    	    4,  // B^2 = Y1^4
    	    3,  // X1 * B
    	    2,  // 4 * X1*B
    	    1,  // 3*A
    	    1,  // E^2
    	    2,  // 2*D
    	    3,  // F - 2*D
    	    3,  // D - X3
    	    3,  // E*(D-X3)
    	    4,  // 8*C
    	    3,  // Final Y subtraction
    	    2,  // 2*Y1*Z1
    	    1   // Final Z
    	};

    public void weightedQuotients() {
    	long weightedLeftWrap = 0;
    	long weightedRightWrap = 0;
    	for (int i = 0; i < 15; i++) {
    	    //weightedLeftWrap += weights[i] * leftWrapCount[i];
    	    //weightedRightWrap += weights[i] * rightWrapCount[i];
    	}
    }

}
