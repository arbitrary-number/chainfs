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
package com.github.chainfs.v4;

import java.math.BigInteger;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

public class FSUtils {

    public static final SecP256K1Curve CURVE = new SecP256K1Curve();
    public static final X9ECParameters CURVE_PARAMS = ECNamedCurveTable.getByName("secp256k1");
    // @formatter:off
    public static final BigInteger Gx =
	        new BigInteger(
	            "79BE667EF9DCBBAC" +
	            "55A06295CE870B07" +
	            "029BFCDB2DCE28D9" +
	            "59F2815B16F81798", 16);

	public static final BigInteger Gy =
	        new BigInteger(
	            "483ADA7726A3C465" +
	            "5DA4FBFC0E1108A8" +
	            "FD17B448A6855419" +
	            "9C47D08FFB10D4B8", 16);
	// @formatter:on
	public static final ECPoint G = CURVE.createPoint(Gx, Gy);
	// Curve params G doesn't work properly for Point addition and subtraction
    //public static final ECPoint CURVE_PARAM_G = CURVE_PARAMS.getG().normalize();
    public static final ECPoint TYPICAL_START_POINT = G;
	private static final BigInteger SATOSHI_X = new BigInteger(
			"05f818748aecbc8c67a4e61a03cee506888f49480cf343363b04908ed51e25b9", 16);
	private static final BigInteger SATOSHI_Y = new BigInteger(
			"615f244c38311983fb0f5b99e3fd52f255c5cc47a03ee2d85e78eaf6fa76bb9d", 16);
    public static final ECPoint S = FSUtils.CURVE.createPoint(SATOSHI_X, SATOSHI_Y);
    public static final ECPoint SATOSHI_START_POINT = S;
    public static final ECPoint SATOSHI_POINT = S;
    public static final BigInteger CURVE_ORDER_MINUS_1_X = new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16);
    public static final BigInteger CURVE_ORDER_MINUS_1_Y = new BigInteger("b7c52588d95c3b9aa25b0403f1eef75702e84bb7597aabe663b82f6f04ef2777", 16);
    public static final ECPoint CURVE_ORDER_MINUS_1 = FSUtils.CURVE.createPoint(CURVE_ORDER_MINUS_1_X, CURVE_ORDER_MINUS_1_Y);;
    public static final BigInteger CURVE_FIELD_SIZE = CURVE.getQ();
    public static final BigInteger P = CURVE_FIELD_SIZE;
    public static final BigInteger PRIME = CURVE_FIELD_SIZE;
    public static final BigInteger CURVE_ORDER = CURVE.getOrder();
    public static final BigInteger ORDER = CURVE_ORDER;
    public static final BigInteger CURVE_MOD = CURVE_ORDER;
    public static final ECPoint INFINITY = CURVE.getInfinity();
    public static final ECPoint SATOSHI_INFINITY = S.subtract(S);

	public static BigInteger modKeyNumberByOrderOfG(BigInteger keyNumber) {
		return keyNumber.mod(CURVE_ORDER);
	}
}
