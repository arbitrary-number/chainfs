/*
 * Copyright (c) Arbitrary Number Project Team. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.github.chainfs.ecc9;

import java.math.BigInteger;

public class Secp256k1EC {

	private static final BigInteger TWO = BigInteger.valueOf(2);

	// secp256k1 field modulus p = 2^256 - 2^32 - 977
    public static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    public static final BigInteger CURVE_ORDER = new BigInteger(
    	    "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

    static BigInteger N;    // Curve order

    public static final BigInteger GX = new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
    public static final BigInteger GY = new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
    public static final BigInteger[] G = new BigInteger[] {GX, GY};       // Generator point

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

    public static Point add(Point P1, Point Q) {
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

        return new Point(X3, Y3, Z3);
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

    public static BigInteger[] scalarDivide(BigInteger divisor, BigInteger x, BigInteger y) {
        // Compute modular inverse of k modulo n
    	try {
    		BigInteger inverseDivisor = divisor.modInverse(CURVE_ORDER);
    		// Multiply point by modular inverse scalar
    		return scalarMultiply(inverseDivisor, x, y);
    	} catch (ArithmeticException e) {
    		throw new IllegalStateException("No mod inverse for: " + divisor);
    	}
    }

    public static BigInteger negateY(BigInteger y) {
        return P.subtract(y).mod(P);
    }

    public static BigInteger[] negate(BigInteger[] Q) {
        return new BigInteger[]{Q[0], P.subtract(Q[1]).mod(P), BigInteger.ONE};
    }

    public static BigInteger[] pointNegate(BigInteger X, BigInteger Y, BigInteger Z, BigInteger P) {
        return new BigInteger[]{X, P.subtract(Y).mod(P), Z};
    }

    // Negate a Jacobian point, returns a Jacobian point
    public static BigInteger[] pointNegate(BigInteger X, BigInteger Y, BigInteger Z) {
        return new BigInteger[]{X, P.subtract(Y).mod(P), Z};
    }

    public static BigInteger[] pointNegateAffine(BigInteger X, BigInteger Y) {
        return new BigInteger[]{X, P.subtract(Y).mod(P)};
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

    public static String pointToKey(BigInteger[] p) {
        byte[] compressed = compressPoint(p[0], p[1]);
        StringBuilder sb = new StringBuilder();
        for (byte b : compressed) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString(); // Proper hex string key
    }

    static class MutableBoolean {
    	public boolean flag;
    }

    public static boolean isEven(BigInteger n) {
        return n.mod(BigInteger.TWO).equals(BigInteger.ZERO);
    }

    private static BigInteger[] inverseFraction(BigInteger[] f) {
		return sFraction(String.valueOf(f[3]), String.valueOf(f[2]));
	}

    /**
     * Attempts to compute the modular inverse of a modulo mod.
     * If inverse does not exist (gcd != 1), returns null and sets factor to gcd.
     *
     * @param a   The number to invert.
     * @param mod The modulus (possibly composite).
     * @return modular inverse of a mod mod, or null if no inverse.
     */
    public static BigInteger modularInverseWithGcdCheck(BigInteger a, BigInteger mod) {
        BigInteger gcd = a.gcd(mod);
        if (!gcd.equals(BigInteger.ONE)) {
            // gcd is a nontrivial factor of mod
            System.out.println("Found factor of modulus: " + gcd);
            return null; // No inverse exists
        }
        // Compute modular inverse using Extended Euclidean Algorithm
        return a.modInverse(mod);
    }

	public static BigInteger[] add(BigInteger[] p1, BigInteger[] p2) {
		if (p2[0].compareTo(BigInteger.ZERO) == 0 &&
			p2[1].compareTo(BigInteger.ZERO) == 0) {
				return new BigInteger[] {p1[0], p1[1]};
		}
		if (p1[0].compareTo(BigInteger.ZERO) == 0 &&
				p1[1].compareTo(BigInteger.ZERO) == 0) {
					return new BigInteger[] {p2[0], p2[1]};
		}
    	BigInteger[] pointAddMixed = pointAddMixed(p1[0], p1[1], BigInteger.ONE,
				p2[0], p2[1]);
		return toAffinePoint(pointAddMixed);
	}

    public static BigInteger[] subtract(BigInteger[] a, BigInteger[] b) {
    	b = pointNegateAffine(b[0], b[1]);
		BigInteger[] pointAddMixed = pointAddMixed(a[0], a[1], BigInteger.ONE,
				b[0], b[1]);
		return toAffinePoint(pointAddMixed);
	}

	// first param is the point, 1, 2, second is the fraction index 2 and 3
    public static BigInteger[] multiply(BigInteger[] a, BigInteger[] b) {
    	BigInteger[] q;
    	if (b[2].compareTo(BigInteger.ZERO) < 0) {
    		BigInteger b2Negated = b[2].negate();
    		BigInteger[] aNegated = negate(a);
    		q = scalarMultiply(b2Negated, aNegated[0], aNegated[1]);
    	} else {
    		q = scalarMultiply(b[2], a[0], a[1]);
    	}
		if (b[3].compareTo(BigInteger.ONE) == 0) {
			//modular inverse of 1 isn't working
			return q;
		}
		BigInteger[] q2 = scalarDivide(b[3], q[0], q[1]);
		return q2;
	}

	public static BigInteger[] fraction(int n, int d) {
        BigInteger gy = GY;
		BigInteger[] N = new BigInteger[] {GX, gy};
		if (n == 1) {
			// skip
		} else if (n > 1) {
        	N = scalarMultiply(v(n), GX, GY);
        } else if (n < 0) {
        	gy = negateY(gy);
        	N = scalarMultiply(v(-n), GX, gy);
        }
        BigInteger[] D = scalarDivide(v(d), N[0], N[1]);
        BigInteger[] expanded = new BigInteger[4];
        expanded[0] = D[0];
        expanded[1] = D[1];
        expanded[2] = v(n);
        expanded[3] = v(d);
		return expanded;
	}

	public static BigInteger[] bFraction(BigInteger nB, BigInteger dB) {
        BigInteger gy = GY;
		BigInteger[] N = new BigInteger[] {GX, gy};
		if (nB.compareTo(BigInteger.ONE) == 0) {
			// skip
		} else if (nB.compareTo(BigInteger.ZERO) > 0) {
        	N = scalarMultiply(nB, GX, GY);
        } else if (nB.compareTo(BigInteger.ZERO) < 0) {
        	gy = negateY(gy);
        	N = scalarMultiply(nB.negate(), GX, gy);
        }
        BigInteger[] D = scalarDivide(dB, N[0], N[1]);
        BigInteger[] expanded = new BigInteger[4];
        expanded[0] = D[0];
        expanded[1] = D[1];
        expanded[2] = nB;
        expanded[3] = dB;
		return expanded;
	}

	public static BigInteger[] sFraction(String n, String d) {
        BigInteger nB = new BigInteger(n);
        BigInteger dB = new BigInteger(d);
        BigInteger gy = GY;
		BigInteger[] N = new BigInteger[] {GX, gy};
		if (nB.compareTo(BigInteger.ONE) == 0) {
			// skip
		} else if (nB.compareTo(BigInteger.ZERO) > 0) {
        	N = scalarMultiply(nB, GX, GY);
        } else if (nB.compareTo(BigInteger.ZERO) < 0) {
        	gy = negateY(gy);
        	N = scalarMultiply(nB.negate(), GX, gy);
        }
        BigInteger[] D = scalarDivide(dB, N[0], N[1]);
        BigInteger[] expanded = new BigInteger[4];
        expanded[0] = D[0];
        expanded[1] = D[1];
        expanded[2] = nB;
        expanded[3] = dB;
		return expanded;
	}

	private static BigInteger v(int i) {
		return BigInteger.valueOf(i);
	}

	private static BigInteger inverseDivisor(BigInteger divisor) {
		return divisor.modInverse(CURVE_ORDER);
	}

	public static BigInteger[] fractionPoint(BigInteger[] p, int i, int j) {
		BigInteger[] f = fraction(i,j);
		return multiply(p,f);
	}

	public static BigInteger[] sFractionPoint(BigInteger[] p,
			BigInteger i, BigInteger j) {
		BigInteger[] f = bFraction(i,j);
		return multiply(p,f);
	}
}
