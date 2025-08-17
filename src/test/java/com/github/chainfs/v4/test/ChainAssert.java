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
 * Copyright (c) Arbitrary Number Project Team. All rights reserved.
 */
package com.github.chainfs.v4.test;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

public class ChainAssert {

	private static final String INFINITY = "Infinity";

	public static void assertEquals(ECPoint a, ECPoint b) {
		String a1 = toString(a);
		String b1 = toString(b);
		if (!a1.equals(b1)) {
			throw new AssertionFailedError("expected: " + a1 + " but was: " + b1);
		}
	}

	public static void assertEquals(BigInteger a, BigInteger b) {
		Assertions.assertEquals(a, b);
	}

	public void assertNotEquals(ECPoint a, ECPoint b) {
		a = a.normalize();
		b = b.normalize();
		String a1 = a.getAffineXCoord().toBigInteger() + "," + a.getAffineYCoord().toBigInteger();
		String b1 = b.getAffineXCoord().toBigInteger() + "," + b.getAffineYCoord().toBigInteger();
		if (!a1.equals(b1)) {
			throw new AssertionFailedError("expected values not to be equal");
		}
	}

	public static String toString(ECPoint a) {
		ECFieldElement x1 = a.normalize().getAffineXCoord();
		ECFieldElement y1 = a.normalize().getAffineYCoord();
		System.out.println("x1 = " + x1);
		System.out.println("y1 = " + y1);
		String a1 = x1 == null ? INFINITY :
			x1.toBigInteger().toString(16) + "," + y1.toBigInteger().toString(16);
		return a1;
	}
}
