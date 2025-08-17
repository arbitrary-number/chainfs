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

import com.github.chainfs.ASTNode;
import com.github.chainfs.v2.CreateNode3;

public class FSOps {

	public FS4DRef encrypt(String key, String value) {
		BigInteger keyNumber = new BigInteger(1, key.getBytes());
		BigInteger modKeyNumber = FSUtils.modKeyNumberByOrderOfG(keyNumber);
		ASTNode gASTNode = CreateNode3.process(modKeyNumber, null);
		ASTNode sASTNode = CreateSatoshiNode.process(keyNumber, null);

		FS3DRef gRef = new FS3DRef();
		gRef.x = gASTNode.getEcPoint().normalize()
					.getAffineXCoord().toBigInteger();
		gRef.gNode = gASTNode.getgCount();
		gRef.sNode = sASTNode.getgCount();

		FS3DRef sRef = new FS3DRef();
		sRef.x = sASTNode.getEcPoint().normalize()
					.getAffineXCoord().toBigInteger();
		sRef.gNode = gASTNode.getgCount();
		sRef.sNode = sASTNode.getgCount();

		FS4DRef fourD = new FS4DRef();
		fourD.gRef = gRef;
		fourD.sRef = sRef;
		return fourD;

	}

}
