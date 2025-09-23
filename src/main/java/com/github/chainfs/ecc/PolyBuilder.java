package com.github.chainfs.ecc;
import java.math.BigInteger;

public class PolyBuilder {

	public static BigInteger[] buildFl(BigInteger l, BigInteger t, BigInteger p) {
	    BigInteger[] f = new BigInteger[3];

	    // constant term = p mod l
	    f[0] = p.mod(l);

	    // x^1 coefficient = -t mod l
	    f[1] = t.negate().mod(l);

	    // x^2 coefficient = 1 (monic)
	    f[2] = BigInteger.ONE;

	    return f;
	}

    /**
     * Original incorrect impl:
     *
     * Build polynomial f(x) = x^2 - t*x + l mod p
     * Returns array [constant term, x^1 coeff, x^2 coeff]
     */
//    public static BigInteger[] buildFl(BigInteger l, BigInteger t, BigInteger p) {
//        BigInteger[] f = new BigInteger[3];
//
//        //BigInteger a = BigInteger.ONE; // coefficient of x^2 is always 1
//       //BigInteger b = tMod.negate().mod(l); // -t mod l
//        //BigInteger c = pMod; // p mod l
//
//        // constant term = l mod p
//        f[0] = l.mod(p);  //l.mod(p);  original
//        //f[0] = p.mod(l);  //l.mod(p);  new?
//
//        // x^1 coefficient = -t mod p
//        //f[1] = t.negate().mod(l);  //p);
//
//        f[1] = p;  //t.negate().mod(l);  //p);
//
//        // x^2 coefficient = 1 (monic)
//        f[2] = BigInteger.ONE;
//
//        return f;
//    }

    public static void main(String[] args) {
    	compute(null);
    }

    public static BigInteger[] compute(BigInteger l) {
        BigInteger p =
               new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F",
        	   16);

        // N is normally unknown when creating secp256k1
        BigInteger N =
              new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141",
        	  16);

        // t is normally unknown when creating secp256k1
        BigInteger t = p.add(BigInteger.ONE).subtract(N);

        System.out.println("secp256k1 prime is: " + p.toString(16));
        System.out.println("secp256k1 order is: " + N.toString(16));
        System.out.println("secp256k1 t is: (p+1)-N");
        System.out.println("secp256k1 t numeric is: " + t.toString(16));
        if (l == null) {
        	l = new BigInteger("11");  // 11 BigInteger.valueOf(11);
    	}
    	System.out.println("ℓ = " + l);
        //BigInteger t = BigInteger.valueOf(3); // example trace mod l, just for demo
    	System.out.println("t = " + t);
        BigInteger[] f = buildFl(l, t, p);

        boolean omit = f[2].compareTo(BigInteger.ONE) == 0;
        System.out.println("f(x) = " + (omit?"":f[2].toString(16)) + "x² - " + f[1].toString(16) + "x + " + f[0]);
        return BigIntPolyFactorization.compute(f, l);
    }
}
