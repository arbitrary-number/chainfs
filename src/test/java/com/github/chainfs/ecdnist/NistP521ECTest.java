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
package com.github.chainfs.ecdnist;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class NistP521ECTest {

    @Test
    void testBasePointIsOnCurve() {
        BigInteger[] G = NistP521EC.G;
        assertTrue(NistP521EC.isOnCurve(G[0], G[1]), "Base point G must be on the curve");
    }

    @Test
    void testPointDoublingIsOnCurve() {
        BigInteger[] G = NistP521EC.G;
        BigInteger[] doubled = NistP521EC.pointDouble(G);
        assertNotNull(doubled, "Doubling result should not be null");
        assertTrue(NistP521EC.isOnCurve(doubled[0], doubled[1]), "2G must be on the curve");
    }

    @Test
    void testPointAdditionIsOnCurve() {
        BigInteger[] G = NistP521EC.G;
        BigInteger[] twoG = NistP521EC.pointAdd(G, G);
        assertNotNull(twoG, "Addition result should not be null");
        assertTrue(NistP521EC.isOnCurve(twoG[0], twoG[1]), "G + G must be on the curve");
    }

    @Test
    void testScalarMultiplication3G() {
        BigInteger[] G = NistP521EC.G;
        BigInteger[] threeG = NistP521EC.scalarMultiply(BigInteger.valueOf(3), G);
        assertNotNull(threeG, "3G result should not be null");
        assertTrue(NistP521EC.isOnCurve(threeG[0], threeG[1]), "3G must be on the curve");
    }

    @Test
    void testScalarMultiplicationWithZeroReturnsInfinity() {
        BigInteger[] G = NistP521EC.G;
        BigInteger[] result = NistP521EC.scalarMultiply(BigInteger.ZERO, G);
        assertNull(result, "0 * G should return point at infinity (null)");
    }

    @Test
    void testScalarMultiplicationWithOneReturnsSamePoint() {
        BigInteger[] G = NistP521EC.G;
        BigInteger[] result = NistP521EC.scalarMultiply(BigInteger.ONE, G);
        assertNotNull(result);
        assertEquals(G[0], result[0], "X coordinate should match base point");
        assertEquals(G[1], result[1], "Y coordinate should match base point");
    }
}
