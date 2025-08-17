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

import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.chainfs.ASTNode;
import com.github.chainfs.v2.CreateNode3;
import com.github.chainfs.v4.CreateSatoshiNode;
import com.github.chainfs.v4.FSUtils;

public class FSUtilsTest {

	private static final ECPoint INFINITY = FSUtils.INFINITY;

	private static final ECPoint SATOSHI_INFINITY = FSUtils.SATOSHI_INFINITY;

	public static final ECPoint CURVE_ORDER_MINUS_1 = FSUtils.CURVE_ORDER_MINUS_1;

	@Test
	public void testModKeyNumberByOrderOfG() {
		BigInteger result = FSUtils.modKeyNumberByOrderOfG(BigInteger.ONE);
		ChainAssert.assertEquals(BigInteger.ONE, result);

		result = FSUtils.modKeyNumberByOrderOfG(BigInteger.TWO);
		ChainAssert.assertEquals(BigInteger.TWO, result);

		BigInteger num = new BigInteger(1, "this is a test".getBytes());
		result = FSUtils.modKeyNumberByOrderOfG(num);
		ChainAssert.assertEquals(num, result);

		String longString = "this is a test of a very long string";
		num = new BigInteger(1, longString.getBytes());
		result = FSUtils.modKeyNumberByOrderOfG(num);

		Assertions.assertNotEquals(num, result);
        // @formatter:off
		BigInteger expected = new BigInteger(
				"22614192655776056068265714430606414564460546" +
				"5249316864268852861068835845348265283907175");
		// @formatter:on
		Assertions.assertNotEquals(expected, result);
	}

	@Test
	public void testOrderOfG() {
		// Test that (Order of G - 1) + 1 = Infinity and that (Infinity) + 1 + G;
		ECPoint oneG = FSUtils.G;
		ECPoint twoG = oneG.add(FSUtils.G);
		ChainAssert.assertEquals(oneG, twoG.subtract(oneG));
		ChainAssert.assertEquals(FSUtils.G, twoG.subtract(oneG));
		Assertions.assertNotEquals(oneG, twoG);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		ECPoint sum = curveOrderMinusOnePoint.normalize().add(oneG);
		Assertions.assertNotEquals(oneG, sum);
		Assertions.assertNotEquals(twoG, sum);
		ChainAssert.assertEquals(INFINITY, sum);
		sum = sum.add(oneG);
		ChainAssert.assertEquals(oneG, sum);
	}

	@Test
	public void testOrderOf3G() {
		// Test that (Order of G - 1) + 1 = Infinity and that (Infinity) + 1 + G;
		ECPoint oneG = FSUtils.G;
		ECPoint twoG = oneG.add(FSUtils.G);
		ECPoint threeG = twoG.add(FSUtils.G);
		ChainAssert.assertEquals(oneG, twoG.subtract(oneG));
		ChainAssert.assertEquals(FSUtils.G, twoG.subtract(oneG));
		ChainAssert.assertEquals(oneG, threeG.subtract(twoG));
		Assertions.assertNotEquals(oneG, twoG);
		Assertions.assertNotEquals(oneG, threeG);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		ECPoint sum = curveOrderMinusOnePoint.normalize().add(oneG);
		Assertions.assertNotEquals(oneG, sum);
		Assertions.assertNotEquals(twoG, sum);
		ChainAssert.assertEquals(INFINITY, sum);
		sum = sum.add(oneG);
		ChainAssert.assertEquals(oneG, sum);
	}

	@Test
	public void testOrderOfSatoshi1() {
		// Test that (Order of S - 1) + 1 = Infinity and that (Infinity) + 1 + S;
		ECPoint oneS = FSUtils.S;
		ECPoint twoS = oneS.add(FSUtils.S);
		ChainAssert.assertEquals(oneS, twoS.subtract(oneS));
		ChainAssert.assertEquals(FSUtils.S, twoS.subtract(oneS));
		Assertions.assertNotEquals(oneS, twoS);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		Assertions.assertTrue(curveOrderMinusOnePoint.isValid());
		ECPoint sum = curveOrderMinusOnePoint.normalize().add(oneS).normalize();
		Assertions.assertTrue(sum.isValid());
		Assertions.assertNotEquals(oneS, sum);
		Assertions.assertNotEquals(twoS, sum);
		ChainAssert.assertEquals(SATOSHI_INFINITY, sum);
		sum = sum.add(oneS);
		ChainAssert.assertEquals(oneS, sum);
	}

	@Test
	public void testOrderOfSatoshi1b() {
		// Test that (Order of S - 1) + 1 = Infinity and that (Infinity) + 1 + S;
		ECPoint oneS = FSUtils.S;
		ECPoint twoS = oneS.add(FSUtils.S);
		ChainAssert.assertEquals(oneS, twoS.subtract(oneS));
		ChainAssert.assertEquals(FSUtils.S, twoS.subtract(oneS));
		Assertions.assertNotEquals(oneS, twoS);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		Assertions.assertTrue(curveOrderMinusOnePoint.isValid());
		ECPoint sum = curveOrderMinusOnePoint.normalize().add(oneS).normalize();
		Assertions.assertTrue(sum.isValid());
		Assertions.assertNotEquals(oneS, sum);
		Assertions.assertNotEquals(twoS, sum);
		ChainAssert.assertEquals(FSUtils.SATOSHI_POINT, sum);
		ChainAssert.assertEquals(SATOSHI_INFINITY, sum);
		sum = sum.add(oneS);
		ChainAssert.assertEquals(oneS, sum);
	}

	@Test
	public void testOrderOfSatoshi2() {
		// Test that (Order of S - 1) + 1 = Infinity and that (Infinity) + 1 + S;
		ECPoint oneS = FSUtils.S;
		ECPoint twoS = oneS.add(FSUtils.S);
		ChainAssert.assertEquals(oneS, twoS.subtract(oneS));
		ChainAssert.assertEquals(FSUtils.S, twoS.subtract(oneS));
		Assertions.assertNotEquals(oneS, twoS);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		Assertions.assertTrue(curveOrderMinusOnePoint.isValid());
		ECPoint sum = curveOrderMinusOnePoint.normalize().add(oneS).add(oneS).normalize();
		Assertions.assertTrue(sum.isValid());
		Assertions.assertNotEquals(oneS, sum);
		Assertions.assertNotEquals(twoS, sum);
		ChainAssert.assertEquals(SATOSHI_INFINITY, sum);
		sum = sum.add(oneS);
		ChainAssert.assertEquals(oneS, sum);
	}

	@Test
	public void testOrderOfSatoshi3() {
		// Test that (Order of S - 1) + 1 = Infinity and that (Infinity) + 1 + S;
		ECPoint oneS = FSUtils.S;
		ECPoint twoS = oneS.add(FSUtils.S);
		ChainAssert.assertEquals(oneS, twoS.subtract(oneS));
		ChainAssert.assertEquals(FSUtils.S, twoS.subtract(oneS));
		Assertions.assertNotEquals(oneS, twoS);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		Assertions.assertTrue(curveOrderMinusOnePoint.isValid());
		ECPoint sum = curveOrderMinusOnePoint.normalize().add(oneS).add(oneS).add(oneS).normalize();
		Assertions.assertTrue(sum.isValid());
		Assertions.assertNotEquals(oneS, sum);
		Assertions.assertNotEquals(twoS, sum);
		ChainAssert.assertEquals(SATOSHI_INFINITY, sum);
		sum = sum.add(oneS);
		ChainAssert.assertEquals(oneS, sum);
	}

	@Test
	public void testOrderOfSatoshi4() {
		// Test that (Order of S - 1) + 1 = Infinity and that (Infinity) + 1 + S;
		ECPoint oneS = FSUtils.S;
		ECPoint twoS = oneS.add(FSUtils.S);
		ChainAssert.assertEquals(oneS, twoS.subtract(oneS));
		ChainAssert.assertEquals(FSUtils.S, twoS.subtract(oneS));
		Assertions.assertNotEquals(oneS, twoS);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		Assertions.assertTrue(curveOrderMinusOnePoint.isValid());
		ECPoint sum = curveOrderMinusOnePoint.normalize().add(oneS)
				.add(oneS).add(oneS).add(oneS).normalize();
		Assertions.assertTrue(sum.isValid());
		Assertions.assertNotEquals(oneS, sum);
		Assertions.assertNotEquals(twoS, sum);
		ChainAssert.assertEquals(SATOSHI_INFINITY, sum);
		sum = sum.add(oneS);
		ChainAssert.assertEquals(oneS, sum);
	}

	@Test
	public void testOrderOfSatoshiN() {
		// Test that (Order of S - 1) + 1 = Infinity and that (Infinity) + 1 + S;
		ECPoint oneS = FSUtils.S;
		ECPoint twoS = oneS.add(FSUtils.S);
		ChainAssert.assertEquals(oneS, twoS.subtract(oneS));
		ChainAssert.assertEquals(FSUtils.S, twoS.subtract(oneS));
		Assertions.assertNotEquals(oneS, twoS);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		Assertions.assertTrue(curveOrderMinusOnePoint.isValid());
		ECPoint sum = curveOrderMinusOnePoint.normalize();

		for (int i=0;i<20;i++) {
			sum = sum.add(oneS).normalize();
			Assertions.assertTrue(sum.isValid());
			System.out.println("x = " + sum.getAffineXCoord().toBigInteger());
			System.out.println("y = " + sum.getAffineYCoord().toBigInteger());
		}
		Assertions.assertTrue(sum.isValid());
		//Assertions.assertNotEquals(oneS, sum);
		//Assertions.assertNotEquals(twoS, sum);
		ChainAssert.assertEquals(SATOSHI_INFINITY, sum);
		sum = sum.add(oneS);
		ChainAssert.assertEquals(oneS, sum);
	}

	@Test
	public void testOrderOfSatoshiCorrected() {
		// Test that (Order of S - 1) + 1 = Infinity and that (Infinity) + 1 + S;
		ECPoint oneS = FSUtils.S;
		ECPoint twoS = oneS.add(FSUtils.S);
		ChainAssert.assertEquals(oneS, twoS.subtract(oneS));
		ChainAssert.assertEquals(FSUtils.S, twoS.subtract(oneS));
		Assertions.assertNotEquals(oneS, twoS);
		ECPoint curveOrderMinusOnePoint = CURVE_ORDER_MINUS_1;
		Assertions.assertTrue(curveOrderMinusOnePoint.isValid());
		ECPoint sum = curveOrderMinusOnePoint.normalize().add(oneS).normalize();
		Assertions.assertTrue(sum.isValid());
		Assertions.assertNotEquals(oneS, sum);
		Assertions.assertNotEquals(twoS, sum);
		ChainAssert.assertEquals(SATOSHI_INFINITY, sum);
		sum = sum.add(oneS);
		ChainAssert.assertEquals(oneS, sum);
	}

	@Test
	public void testSatoshiInfinity() {
		ChainAssert.assertEquals(INFINITY, SATOSHI_INFINITY);
		ECPoint gInifinityFromSatoshi = FSUtils.S.subtract(FSUtils.G);
		Assertions.assertNotEquals(INFINITY, gInifinityFromSatoshi);
		Assertions.assertNotEquals(SATOSHI_INFINITY, gInifinityFromSatoshi);
	}
}
