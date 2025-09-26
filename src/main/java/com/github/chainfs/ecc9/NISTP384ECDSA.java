package com.github.chainfs.ecc9;
import java.math.BigInteger;
import java.security.SecureRandom;

public class NISTP384ECDSA {

    // NIST P-384 curve parameters (from FIPS 186-4)
    private static final BigInteger p = new BigInteger(
        "39402006196394479212279040100143613805079739270465446667948293404245721771496870329047266088258938001861606973112319");
    private static final BigInteger a = new BigInteger(
        "39402006196394479212279040100143613805079739270465446667948293404245721771496870329047266088258938001861606973112316");
    private static final BigInteger b = new BigInteger(
        "2758019355995970587784901184038904809305690585636156851859984789222565518657039532736447567559084803210947145401434");
    public static final BigInteger n = new BigInteger(
        "39402006196394479212279040100143613805079739270465446667946905279627659399113263569398956308152294913554433653942643");
    private static final BigInteger h = BigInteger.ONE;

    // Base point G
    private static final BigInteger Gx = new BigInteger(
        "262470350957996892686231567445669818918529234911092133878156159143631564581264057974512640204691201504757157150376");
    private static final BigInteger Gy = new BigInteger(
        "832571096148902998554675128952010817928785304886131559470920590248050319988441922109253522505279899881839844012124");

    // Private key d and public key Q = dG
    private BigInteger d;
    private ECPoint Q;

    private static final SecureRandom random = new SecureRandom();

    public NISTP384ECDSA() {
        // Generate random private key d in [1, n-1]
        do {
            d = new BigInteger(n.bitLength(), random);
        } while (d.compareTo(BigInteger.ONE) < 0 || d.compareTo(n) >= 0);

        // Calculate public key Q = dG
        Q = scalarMultiply(new ECPoint(Gx, Gy), d);
    }

    // Signing a message hash z (should be a hash integer mod n)
    public Signature sign(BigInteger z) {
        BigInteger k, r, s;

        do {
            do {
                k = new BigInteger(n.bitLength(), random);
            } while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(n) >= 0);

            ECPoint p = scalarMultiply(new ECPoint(Gx, Gy), k);
            r = p.x.mod(n);
        } while (r.equals(BigInteger.ZERO));

        BigInteger kInv = k.modInverse(n);
        s = kInv.multiply(z.add(d.multiply(r))).mod(n);

        if (s.equals(BigInteger.ZERO)) {
            return sign(z); // retry if s=0
        }
        return new Signature(r, s);
    }

    // Verify signature (r, s) on message hash z
    public boolean verify(BigInteger z, Signature sig) {
        BigInteger r = sig.r;
        BigInteger s = sig.s;

        if (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(n) >= 0) return false;
        if (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(n) >= 0) return false;

        BigInteger w = s.modInverse(n);
        BigInteger u1 = z.multiply(w).mod(n);
        BigInteger u2 = r.multiply(w).mod(n);

        ECPoint G = new ECPoint(Gx, Gy);
        ECPoint point = pointAdd(scalarMultiply(G, u1), scalarMultiply(Q, u2));
        if (point.isInfinity()) return false;

        BigInteger v = point.x.mod(n);
        return v.equals(r);
    }

    // Elliptic curve point addition
    private static ECPoint pointAdd(ECPoint P, ECPoint Q) {
        if (P.isInfinity()) return Q;
        if (Q.isInfinity()) return P;

        BigInteger lambda;
        if (P.x.equals(Q.x)) {
            if (P.y.add(Q.y).mod(p).equals(BigInteger.ZERO)) {
                return ECPoint.INFINITY;
            }
            // Point doubling
            BigInteger numerator = P.x.pow(2).multiply(BigInteger.valueOf(3)).add(a).mod(p);
            BigInteger denominator = P.y.multiply(BigInteger.valueOf(2)).modInverse(p);
            lambda = numerator.multiply(denominator).mod(p);
        } else {
            // Point addition
            BigInteger numerator = Q.y.subtract(P.y).mod(p);
            BigInteger denominator = Q.x.subtract(P.x).mod(p).modInverse(p);
            lambda = numerator.multiply(denominator).mod(p);
        }

        BigInteger xr = lambda.pow(2).subtract(P.x).subtract(Q.x).mod(p);
        BigInteger yr = lambda.multiply(P.x.subtract(xr)).subtract(P.y).mod(p);

        return new ECPoint(xr, yr);
    }

    // Elliptic curve scalar multiplication (double-and-add)
    private static ECPoint scalarMultiply(ECPoint P, BigInteger k) {
        ECPoint R = ECPoint.INFINITY;
        ECPoint addend = P;

        for (int i = k.bitLength() - 1; i >= 0; i--) {
            R = pointAdd(R, R);
            if (k.testBit(i)) {
                R = pointAdd(R, addend);
            }
        }
        return R;
    }

    // Point representation
    private static class ECPoint {
        BigInteger x, y;
        static final ECPoint INFINITY = new ECPoint(null, null);

        ECPoint(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        boolean isInfinity() {
            return x == null && y == null;
        }
    }

    // Signature pair
    public static class Signature {
        public final BigInteger r;
        public final BigInteger s;

        public Signature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }
    }

    // For testing convenience: get public key coordinates
    public BigInteger getPublicKeyX() {
        return Q.x;
    }

    public BigInteger getPublicKeyY() {
        return Q.y;
    }

    public BigInteger getPrivateKey() {
        return d;
    }
}
