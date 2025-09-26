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

public class Point {

	// secp256k1 field and curve parameters
	static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
	static final BigInteger N = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
	static final BigInteger Gx = new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
	static final BigInteger Gy = new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);


	BigInteger X, Y, Z;

	public Point(BigInteger x, BigInteger y) {
		this.X = x;
		this.Y = y;
		this.Z = BigInteger.ONE;
	}

	public Point(BigInteger X, BigInteger Y, BigInteger Z) {
		this.X = X;
		this.Y = Y;
		this.Z = Z;
	}

	public boolean isInfinity() {
		return Z.equals(BigInteger.ZERO);
	}

	public Point add(Point Q) {
		if (this.isInfinity()) return Q;
		if (Q.isInfinity()) return this;

		BigInteger Z1Z1 = Z.multiply(Z).mod(P);
		BigInteger Z2Z2 = Q.Z.multiply(Q.Z).mod(P);

		BigInteger U1 = X.multiply(Z2Z2).mod(P);
		BigInteger U2 = Q.X.multiply(Z1Z1).mod(P);

		BigInteger Z1Cubed = Z.multiply(Z1Z1).mod(P);
		BigInteger Z2Cubed = Q.Z.multiply(Z2Z2).mod(P);

		BigInteger S1 = Y.multiply(Z2Cubed).mod(P);
		BigInteger S2 = Q.Y.multiply(Z1Cubed).mod(P);

		if (U1.equals(U2)) {
			if (S1.equals(S2)) {
				return this.doublePoint();
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
		BigInteger Z3 = H.multiply(Z).multiply(Q.Z).mod(P);

		// Residue info
		System.out.printf("Residues: U1=%d, U2=%d, S1=%d, S2=%d, H=%d, R=%d%n",
				legendre(U1), legendre(U2), legendre(S1), legendre(S2), legendre(H), legendre(R));

		return new Point(X3, Y3, Z3);
	}

	public Point doublePoint() {
		if (this.isInfinity()) return this;

		BigInteger A = X.multiply(X).mod(P); // XX
		BigInteger B = Y.multiply(Y).mod(P); // YY
		BigInteger C = B.multiply(B).mod(P); // YYYY

		BigInteger D = X.add(B).mod(P).multiply(B).mod(P).multiply(BigInteger.TWO).mod(P);
		BigInteger E = A.multiply(BigInteger.valueOf(3)).mod(P); // 3*X^2
		BigInteger F = E.multiply(E).subtract(D.multiply(BigInteger.TWO)).mod(P);

		BigInteger X3 = F;
		BigInteger Y3 = E.multiply(D.subtract(F)).subtract(C.multiply(BigInteger.valueOf(8))).mod(P);
		BigInteger Z3 = Y.multiply(Z).multiply(BigInteger.TWO).mod(P);

		return new Point(X3, Y3, Z3);
	}

	int legendre(BigInteger a) {
		if (a.equals(BigInteger.ZERO)) return 0;
		return a.modPow(P.subtract(BigInteger.ONE).divide(BigInteger.TWO), P).equals(BigInteger.ONE) ? 1 : 0;
	}

	public Point toAffine() {
		if (isInfinity()) return new Point(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
		BigInteger Zinv = Z.modInverse(P);
		BigInteger Z2 = Zinv.multiply(Zinv).mod(P);
		BigInteger Z3 = Z2.multiply(Zinv).mod(P);
		BigInteger x = X.multiply(Z2).mod(P);
		BigInteger y = Y.multiply(Z3).mod(P);
		return new Point(x, y);
	}
}