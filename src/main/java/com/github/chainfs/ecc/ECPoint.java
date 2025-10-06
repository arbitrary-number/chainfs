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
package com.github.chainfs.ecc;

import java.math.BigInteger;
import java.util.Objects;

public class ECPoint {
    private final BigInteger x;
    private final BigInteger y;
    private final ECCurve curve;

    public ECPoint(BigInteger x, BigInteger y, ECCurve curve) {
        this.x = x;
        this.y = y;
        this.curve = curve;
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public boolean isInfinity() {
        return x == null && y == null;
    }

    public boolean isOnCurve() {
        if (isInfinity()) return true;

        BigInteger p = curve.getP();
        BigInteger a = curve.getA();
        BigInteger b = curve.getB();

        BigInteger lhs = y.modPow(BigInteger.TWO, p);
        BigInteger rhs = x.modPow(BigInteger.valueOf(3), p)
                          .add(a.multiply(x)).add(b).mod(p);
        return lhs.equals(rhs);
    }

    public ECPoint negate() {
        if (isInfinity()) return this;
        BigInteger p = curve.getP();
        return new ECPoint(x, p.subtract(y).mod(p), curve);
    }

    public ECPoint add(ECPoint q) {
        if (!curve.equals(q.curve)) {
            throw new IllegalArgumentException("Points are not on the same curve");
        }

        if (this.isInfinity()) return q;
        if (q.isInfinity()) return this;

        BigInteger p = curve.getP();

        if (x.equals(q.x)) {
            if (!y.equals(q.y)) {
                return curve.getInfinity(); // P + (-P) = O
            } else {
                return doublePoint(); // P + P
            }
        }

        BigInteger lambda = q.y.subtract(y)
                .multiply(q.x.subtract(x).modInverse(p))
                .mod(p);

        BigInteger xr = lambda.pow(2).subtract(x).subtract(q.x).mod(p);
        BigInteger yr = lambda.multiply(x.subtract(xr)).subtract(y).mod(p);

        return new ECPoint(xr, yr, curve);
    }

    public ECPoint doublePoint() {
        if (isInfinity()) return this;

        BigInteger p = curve.getP();
        BigInteger a = curve.getA();

        if (y.equals(BigInteger.ZERO)) {
            return curve.getInfinity(); // tangent is vertical
        }

        BigInteger lambda = x.pow(2).multiply(BigInteger.valueOf(3)).add(a)
                .multiply(y.multiply(BigInteger.TWO).modInverse(p))
                .mod(p);

        BigInteger xr = lambda.pow(2).subtract(x.multiply(BigInteger.TWO)).mod(p);
        BigInteger yr = lambda.multiply(x.subtract(xr)).subtract(y).mod(p);

        return new ECPoint(xr, yr, curve);
    }

    public ECPoint multiply(BigInteger k) {
        ECPoint result = curve.getInfinity();
        ECPoint addend = this;

        while (k.signum() != 0) {
            if (k.testBit(0)) {
                result = result.add(addend);
            }
            addend = addend.doublePoint();
            k = k.shiftRight(1);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ECPoint)) return false;
        ECPoint other = (ECPoint) obj;
        if (this.isInfinity() && other.isInfinity()) return true;
        return Objects.equals(this.x, other.x)
            && Objects.equals(this.y, other.y)
            && this.curve.equals(other.curve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, curve);
    }

    @Override
    public String toString() {
        if (isInfinity()) return "Point(Infinity)";
        return "Point(" + x.toString(16) + ", " + y.toString(16) + ")";
    }
}
