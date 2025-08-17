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

import org.bouncycastle.math.ec.ECPoint;

public class SatoshiPoint {

//	Satoshi Nakamoto's Point on the Curve::
//  Legacy Address: 1DSXoMQeV4REBL9a9U6pGnSQGwgPh9CM13
//	Public key:
//	04
//	05f818748aecbc8c67a4e61a03cee506888f49480cf343363b04908ed51e25b9
//	615f244c38311983fb0f5b99e3fd52f255c5cc47a03ee2d85e78eaf6fa76bb9d

	private static final BigInteger SATOSHI_X = new BigInteger(
			"05f818748aecbc8c67a4e61a03cee506888f49480cf343363b04908ed51e25b9", 16);

	private static final BigInteger SATOSHI_Y = new BigInteger(
			"615f244c38311983fb0f5b99e3fd52f255c5cc47a03ee2d85e78eaf6fa76bb9d", 16);

	public static ECPoint getSatoshiPoint() {
	    ECPoint S = FSUtils.CURVE.createPoint(SATOSHI_X, SATOSHI_Y);
	    return S;
	}

}
