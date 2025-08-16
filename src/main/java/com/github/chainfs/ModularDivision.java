/*
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
 * Copyright (c) Arbitrary Project Team. All rights reserved.
 */
package com.github.chainfs;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

/*
 * Modular Division of an ECPoint in O(1) time
 */
public class ModularDivision {

	private static BigInteger TWO = new BigInteger("2");

	/*
	 * Divides a point, i.e. works out how many portions there are
	 *
	 * @return BigInteger.ZERO if division wasn't successful
	 */
	public BigInteger divide3(ECPoint point) {
		BigInteger divisor = null;
		for (int i=255;i>1;i++) {
			divisor = TWO.pow(i);
			BigInteger result = divide4(point, divisor);
			if (!result.equals(BigInteger.ZERO)) {
				return new BigInteger(String.valueOf(i));
			}
		}
		return divisor;
	}

	/*
	 * Divides a point, i.e. works out how many portions there are
	 *
	 * @return BigInteger.ZERO if division wasn't successful
	 */
	public BigInteger divide4(ECPoint point, BigInteger divisor) {
		if (!checkValid(divisor)) {
			throw new IllegalStateException("Can only divide by a power of 2");
		}
		SecP256K1Curve curve = new SecP256K1Curve();
		ECPoint gPoint = CreateNode2.getgPoint(curve);
		ECPoint gPortionSize = convertToGNumber(divisor);
		ECPoint currentPoint = point;
		for (int i=0;i<256;i++) {
			currentPoint = point.subtract(gPortionSize);
			if (currentPoint.equals(gPoint)) {
				return new BigInteger(String.valueOf(i));
			}
		}
		return BigInteger.ZERO;
	}

	public ECPoint convertToGNumber(BigInteger portionSize) {
		return CreateNode2.process(portionSize, null).getEcPoint();
	}

	public boolean checkValid(BigInteger n) {
		return isPowerOfTwo(n);
	}

	public boolean isPowerOfTwo(BigInteger n) {
		return n.signum() > 0 && !n.equals(BigInteger.ONE) && n.bitCount() == 1;
	}

}
