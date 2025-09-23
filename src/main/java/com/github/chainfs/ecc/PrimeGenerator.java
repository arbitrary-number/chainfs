package com.github.chainfs.ecc;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PrimeGenerator {

	private static final BigInteger P = new BigInteger(
	        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    public static List<Integer> generatePrimes(int max) {
        boolean[] isComposite = new boolean[max + 1];
        List<Integer> primes = new ArrayList<>();

        for (int i = 2; i <= max; i++) {
            if (!isComposite[i]) {
                primes.add(i);
                for (int j = i * 2; j <= max; j += i) {
                    isComposite[j] = true;
                }
            }
        }
        return primes;
    }

 // Compute integer square root of BigInteger using binary search
    public static BigInteger sqrt(BigInteger x) {
        BigInteger right = x, left = BigInteger.ZERO, mid;
        while (right.subtract(left).compareTo(BigInteger.ONE) > 0) {
            mid = left.add(right).shiftRight(1);
            if (mid.multiply(mid).compareTo(x) <= 0) {
                left = mid;
            } else {
                right = mid;
            }
        }
        return left;
    }

    public static void main(String[] args) {
        BigInteger p =
                new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F",
         	   16);

        //System.out.println("p = " + p);
        //System.out.println("Hasse’s bound = Java code to calculate 4 * p^-2 required");

        BigInteger sqrtP = sqrt(p);
        BigInteger hasseBound  = sqrtP.multiply(BigInteger.valueOf(4));
        System.out.println("Hasse’s bound  4 * sqrt(p) = " + hasseBound.toString(16));

        int bound = 100000;  // or higher depending on your needs
        List<Integer> primes = generatePrimes(bound);

        int countBound = 47;
        ArrayList<Integer> requiredPrimes = new ArrayList<Integer>();
        // Multiply primes until product > bound
        BigInteger product = BigInteger.ONE;
        int count = 0;
        for (int prime : primes) {
            product = product.multiply(BigInteger.valueOf(prime));
            count++;
            requiredPrimes.add(prime);
            if (count == countBound)
            	break;
//            if (product.compareTo(hasseBound) > 0) {
//                System.out.println("Product of first " + count + " primes exceeds 4*sqrt(p)");
//                break;
//            }
        }
        System.out.println("Product = " + product.toString(16));
        System.out.println("Number of primes used: " + count);
        System.out.println("Product of primes: " + product.toString(16));

        //System.out.println("Primes up to " + bound + ": " + primes);
        System.out.println("Required primes: " + requiredPrimes);

        ArrayList<BigInteger[]> results = new ArrayList<>();
        ArrayList<BigInteger> elkiesPrimes = new ArrayList<>();
        int i = -1;
        for (Integer requiredPrime : requiredPrimes) {
        	i++;
        	BigInteger[] result = PolyBuilder.compute(new BigInteger(requiredPrime.toString()));
        	results.add(result);
        	if (result != null) {
        		elkiesPrimes.add(new BigInteger(requiredPrime.toString()));
        	}
        }

        //ArrayList<Integer> requiredPrimes = new ArrayList<Integer>();
        // Multiply primes until product > bound
        product = BigInteger.ONE;
        count = 0;
        boolean highEnough = false;
        for (BigInteger elkiePrime: elkiesPrimes) {
            product = product.multiply(elkiePrime);
            count++;
            if (product.compareTo(hasseBound) > 0) {
                System.out.println("Product of first " + count + " primes exceeds 4*sqrt(p)");
                highEnough = true;
                break;
            }
        }
        System.out.println("Elkies Product = " + product.toString(16));
       // System.out.println("Number of primes used: " + count);
        //System.out.println("Product of primes: " + product.toString(16));
        if (!highEnough) {
        	throw new IllegalStateException("not enough primes");
        }

        for (BigInteger[] result : results) {
        	if (result != null) {
        		if (result.length == 1) {
        			System.out.println("Eigenvalues mod " + result[0] + ": none detected");
        		} else {
        			System.out.println("Eigenvalues mod " + result[0] + ":"
        				+ result[1] + "," + result[2]);
        		}
        	}
        }

        ArrayList<BigInteger[]> congruenceSet1 = new ArrayList<BigInteger[]>();
        ArrayList<BigInteger[]> congruenceSet2 = new ArrayList<BigInteger[]>();


        for (BigInteger[] result : results) {
        	if (result != null) {
        		if (result.length == 1) {
        			System.out.println("Eigenvalues mod " + result[0] + ": none detected");
        		} else {
        			System.out.println("Frobenius Eigenvalues mod for Elkie prime " + result[0] + ":"
        				+ result[1] + "," + result[2]);
        			congruenceSet1.add(new BigInteger[] {result[1], result[0]});
        			congruenceSet2.add(new BigInteger[] {result[2], result[0]});
        		}
        	}
        }

        System.out.println("Congurence set 1:");
        ArrayList<BigInteger> aList = new ArrayList<BigInteger>();
        ArrayList<BigInteger> mList = new ArrayList<BigInteger>();
        for (BigInteger[] congruence : congruenceSet1) {
        	System.out.println("Congruence: " + congruence[0] + " mod " + congruence[1]);
        	aList.add(congruence[0]);
        	mList.add(congruence[1]);
		}
        ChineseRemainderTheorem.compute(
        		aList.toArray(new BigInteger[0]),
        		mList.toArray(new BigInteger[0]));

        aList = new ArrayList<BigInteger>();
        mList = new ArrayList<BigInteger>();
        System.out.println("Congurence set 2:");
        for (BigInteger[] congruence : congruenceSet2) {
        	System.out.println("Congruence: " + congruence[0] + " mod " + congruence[1]);
        	aList.add(congruence[0]);
        	mList.add(congruence[1]);
		}
        ChineseRemainderTheorem.compute(
        		aList.toArray(new BigInteger[0]),
        		mList.toArray(new BigInteger[0]));

        processAndCompute(congruenceSet1, congruenceSet2);
    }

    public static void processAndCompute(ArrayList<BigInteger[]> congruenceSet1, ArrayList<BigInteger[]> congruenceSet2) {

        System.out.println("Congruence set 1 (converted to trace):");
        ArrayList<BigInteger> aList = new ArrayList<>();
        ArrayList<BigInteger> mList = new ArrayList<>();
        for (BigInteger[] congruence : congruenceSet1) {
            BigInteger lambda = congruence[0];
            BigInteger mod = congruence[1];
            BigInteger lambdaInv = lambda.modInverse(mod);
            BigInteger traceMod = lambda.add(P.mod(mod).multiply(lambdaInv)).mod(mod);

            System.out.println("Congruence: t ≡ " + traceMod + " mod " + mod);
            aList.add(traceMod);
            mList.add(mod);
        }
        ChineseRemainderTheorem.compute(
            aList.toArray(new BigInteger[0]),
            mList.toArray(new BigInteger[0]));

        System.out.println("Congruence set 2 (converted to trace):");
        aList = new ArrayList<>();
        mList = new ArrayList<>();
        for (BigInteger[] congruence : congruenceSet2) {
            BigInteger lambda = congruence[0];
            BigInteger mod = congruence[1];
            BigInteger lambdaInv = lambda.modInverse(mod);
            BigInteger traceMod = lambda.add(P.mod(mod).multiply(lambdaInv)).mod(mod);

            System.out.println("Congruence: t ≡ " + traceMod + " mod " + mod);
            aList.add(traceMod);
            mList.add(mod);
        }
        ChineseRemainderTheorem.compute(
            aList.toArray(new BigInteger[0]),
            mList.toArray(new BigInteger[0]));
    }
}
