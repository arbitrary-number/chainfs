package com.github.chainfs.ecc;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PrimeGenerator {
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

        int bound = 1000;  // or higher depending on your needs
        List<Integer> primes = generatePrimes(bound);

        ArrayList<Integer> requiredPrimes = new ArrayList<Integer>();
        // Multiply primes until product > bound
        BigInteger product = BigInteger.ONE;
        int count = 0;
        for (int prime : primes) {
            product = product.multiply(BigInteger.valueOf(prime));
            count++;
            requiredPrimes.add(prime);
            if (product.compareTo(hasseBound) > 0) {
                System.out.println("Product of first " + count + " primes exceeds 4*sqrt(p)");
                break;
            }
        }

        System.out.println("Number of primes used: " + count);
        System.out.println("Product of primes: " + product.toString(16));

        //System.out.println("Primes up to " + bound + ": " + primes);
        System.out.println("Required primes: " + requiredPrimes);

        ArrayList<BigInteger[]> results = new ArrayList<>();
        int i = -1;
        for (Integer requiredPrime : requiredPrimes) {
        	i++;
        	BigInteger[] result = PolyBuilder.compute(new BigInteger(requiredPrime.toString()));
        	results.add(result);
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
    }
}
