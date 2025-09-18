package com.github.chainfs.ecc;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

public class Secp256k1EC {

    // secp256k1 field modulus p = 2^256 - 2^32 - 977
    static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    private static final BigInteger CURVE_ORDER = new BigInteger(
    	    "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

    // Helper: modulo with wraparound
    static BigInteger mod(BigInteger x) {
        x = x.mod(P);
        return x.signum() < 0 ? x.add(P) : x;
    }

    static BigInteger modAdd(BigInteger a, BigInteger b) {
        return mod(a.add(b));
    }

    static BigInteger modSub(BigInteger a, BigInteger b) {
        return mod(a.subtract(b));
    }

    static BigInteger modMul(BigInteger a, BigInteger b) {
        return mod(a.multiply(b));
    }

    static BigInteger modSqr(BigInteger a) {
        return mod(a.multiply(a));
    }

    // Point doubling in Jacobian coordinates
    public static BigInteger[] pointDouble(BigInteger X1, BigInteger Y1, BigInteger Z1) {
        BigInteger A = modSqr(X1);                             // A = X1^2
        BigInteger B = modSqr(Y1);                             // B = Y1^2
        BigInteger C = modSqr(B);                              // C = B^2

        BigInteger D = modMul(BigInteger.valueOf(4), modMul(X1, B)); // D = 4 * X1 * B
        BigInteger E = modMul(BigInteger.valueOf(3), A);             // E = 3 * A
        BigInteger F = modSqr(E);                                    // F = E^2

        BigInteger X3 = modSub(F, modMul(BigInteger.valueOf(2), D)); // X3 = F - 2*D
        BigInteger Y3 = modSub(modMul(E, modSub(D, X3)), modMul(BigInteger.valueOf(8), C)); // Y3 = E*(D - X3) - 8*C
        BigInteger Z3 = modMul(BigInteger.valueOf(2), modMul(Y1, Z1)); // Z3 = 2 * Y1 * Z1

        return new BigInteger[]{X3, Y3, Z3};
    }

    // Mixed point addition (Jacobian + affine)
    public static BigInteger[] pointAddMixed(BigInteger X1, BigInteger Y1, BigInteger Z1,
                                             BigInteger x2, BigInteger y2) {
        BigInteger Z1Z1 = modSqr(Z1);
        BigInteger U2 = modMul(x2, Z1Z1);
        BigInteger S2 = modMul(y2, modMul(Z1, Z1Z1));
        BigInteger H = modSub(U2, X1);
        BigInteger r = modSub(S2, Y1);

        if (H.signum() == 0) {
            if (r.signum() == 0) {
                return pointDouble(X1, Y1, Z1); // P == Q
            } else {
                return new BigInteger[]{BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO}; // Point at infinity
            }
        }

        BigInteger H2 = modSqr(H);
        BigInteger H3 = modMul(H, H2);
        BigInteger V = modMul(X1, H2);

        BigInteger X3 = modSub(modSub(modSqr(r), H3), modMul(BigInteger.valueOf(2), V));
        BigInteger Y3 = modSub(modMul(r, modSub(V, X3)), modMul(Y1, H3));
        BigInteger Z3 = modMul(Z1, H);

        return new BigInteger[]{X3, Y3, Z3};
    }

    // Convert Jacobian point to affine coordinates
    public static BigInteger[] toAffine(BigInteger X, BigInteger Y, BigInteger Z) {
        if (Z.equals(BigInteger.ZERO)) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ZERO}; // Point at infinity
        }
        BigInteger Zinv = Z.modInverse(P);
        BigInteger Zinv2 = modSqr(Zinv);
        BigInteger Zinv3 = modMul(Zinv2, Zinv);
        BigInteger x = mod(modMul(X, Zinv2));
        BigInteger y = mod(modMul(Y, Zinv3));
        return new BigInteger[]{x, y};
    }

    // Print point in affine form
    public static void printPoint(BigInteger[] point) {
        System.out.println("x = " + point[0].toString(16));
        System.out.println("y = " + point[1].toString(16));
    }

    public static void printPointExpanded(BigInteger x, BigInteger y) {
        System.out.println("x = " + x.toString(16));
        System.out.println("y = " + y.toString(16));
    }

 // Non-constant-time scalar multiplication: k * G
    public static BigInteger[] scalarMultiply(BigInteger k, BigInteger x, BigInteger y) {
        BigInteger[] result = null; // Null means "point at infinity"
        BigInteger[] base = new BigInteger[]{x, y, BigInteger.ONE};

        int length = k.bitLength();

        for (int i = length - 1; i >= 0; i--) {
            if (result != null) {
                result = pointDouble(result[0], result[1], result[2]); // Always double
            }

            if (k.testBit(i)) {
                if (result == null) {
                    // First set bit: initialize result
                    result = new BigInteger[]{base[0], base[1], base[2]};
                } else {
                    // Add base to result
                    result = pointAddMixed(result[0], result[1], result[2], base[0], base[1]);
                }
            }
        }

        // If still null, the result is point at infinity
        if (result == null) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ZERO};
        }

        return toAffine(result[0], result[1], result[2]);
    }

    public static BigInteger[] scalarDivide(BigInteger k, BigInteger x, BigInteger y) {
        // Compute modular inverse of k modulo n
        BigInteger kInv = k.modInverse(CURVE_ORDER);
        // Multiply point by modular inverse scalar
        return scalarMultiply(kInv, x, y);
    }


    public static BigInteger[] pointNegate(BigInteger X, BigInteger Y, BigInteger Z, BigInteger p) {
        return new BigInteger[]{X, p.subtract(Y).mod(p), Z};
    }

    public static byte[] compressPoint(BigInteger x, BigInteger y) {
        byte[] xBytes = x.toByteArray();
        // Ensure xBytes is exactly 32 bytes
        if (xBytes.length > 32) {
            xBytes = java.util.Arrays.copyOfRange(xBytes, xBytes.length - 32, xBytes.length);
        } else if (xBytes.length < 32) {
            byte[] tmp = new byte[32];
            System.arraycopy(xBytes, 0, tmp, 32 - xBytes.length, xBytes.length);
            xBytes = tmp;
        }
        byte prefix = y.testBit(0) ? (byte) 0x03 : (byte) 0x02;
        byte[] compressed = new byte[33];
        compressed[0] = prefix;
        System.arraycopy(xBytes, 0, compressed, 1, 32);
        return compressed;
    }

    public static BigInteger modSqrt(BigInteger a, BigInteger p) {
        if (a.signum() == 0) return BigInteger.ZERO;
        if (p.testBit(0) && p.testBit(1)) {
            // p % 4 == 3 shortcut
            BigInteger sqrt = a.modPow(p.shiftRight(2).add(BigInteger.ONE), p);
            if (sqrt.modPow(BigInteger.TWO, p).compareTo(a.mod(p)) == 0) {
                return sqrt;
            } else {
                return null; // No sqrt exists
            }
        }
        // For other primes, full Tonelli-Shanks would be needed (omitted here for brevity)
        throw new UnsupportedOperationException("Tonelli-Shanks not implemented for this prime");
    }

    public static BigInteger modSqrtScalar(BigInteger k, BigInteger modulus) {
        if (k.signum() == 0) return BigInteger.ZERO;

        // Check Legendre symbol (optional, for efficiency)
        // Only compute sqrt if k is a quadratic residue mod modulus
        BigInteger legendre = k.modPow(modulus.subtract(BigInteger.ONE).divide(BigInteger.TWO), modulus);
        if (!legendre.equals(BigInteger.ONE)) {
            return null; // No square root exists
        }

        // Since modulus ≡ 3 mod 4, use the fast method:
        BigInteger exp = modulus.add(BigInteger.ONE).shiftRight(2);
        BigInteger root = k.modPow(exp, modulus);

        // Check that root^2 ≡ k mod modulus (to be safe)
        if (root.multiply(root).mod(modulus).compareTo(k.mod(modulus)) != 0) {
            return null; // Shouldn't happen unless implementation error
        }

        return root;
    }


    public static BigInteger[] decompressPoint(byte[] compressed) throws IllegalArgumentException {
        if (compressed.length != 33) {
            throw new IllegalArgumentException("Invalid compressed point length");
        }
        byte prefix = compressed[0];
        if (prefix != 0x02 && prefix != 0x03) {
            throw new IllegalArgumentException("Invalid compressed point prefix");
        }
        byte[] xBytes = new byte[32];
        System.arraycopy(compressed, 1, xBytes, 0, 32);
        BigInteger x = new BigInteger(1, xBytes);

        // Compute y^2 = x^3 + 7 mod p
        BigInteger y2 = mod(x.modPow(BigInteger.valueOf(3), P).add(BigInteger.valueOf(7)));

        // Compute modular sqrt of y2 mod p using Tonelli-Shanks or similar
        BigInteger y = modSqrt(y2, P);

        if (y == null) {
            throw new IllegalArgumentException("Invalid point compression: no square root found");
        }

        // Use prefix to determine correct y parity
        boolean yIsOdd = y.testBit(0);
        if ((prefix == 0x03) != yIsOdd) {
            y = P.subtract(y); // Choose the other root
        }

        return new BigInteger[] { x, y };
    }

    public static BigInteger[][] precomputeMultiples(BigInteger Gx,
    		BigInteger Gy, int size) {
        BigInteger[][] table = new BigInteger[size][2]; // Each entry is affine point {x, y}

        // Start with 1*G
        table[0] = new BigInteger[]{Gx, Gy};

        // Compute each subsequent multiple by adding G
        for (int i = 1; i < size; i++) {
            // Convert previous point to Jacobian coords (X, Y, Z=1)
            BigInteger[] prevJac = new BigInteger[]{table[i-1][0], table[i-1][1], BigInteger.ONE};

            // Add base point G in affine coords to previous
            BigInteger[] sumJac = pointAddMixed(prevJac[0], prevJac[1], prevJac[2], Gx, Gy);

            // Convert back to affine coords
            BigInteger[] sumAffine = toAffine(sumJac[0], sumJac[1], sumJac[2]);

            table[i] = sumAffine;
        }

        return table;
    }

    public static String pointToKey(BigInteger[] p) {
        byte[] compressed = compressPoint(p[0], p[1]);
        StringBuilder sb = new StringBuilder();
        for (byte b : compressed) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString(); // Proper hex string key
    }

    public static void testPointCompression() {
        System.out.println("\n--- testPointCompression ---");

        BigInteger k = new BigInteger("1234567890123456789012345678901234567890");
        BigInteger[] kG = scalarMultiply(k, GX, GY);

        byte[] compressed = compressPoint(kG[0], kG[1]);
        //System.out.println("Compressed point: " + javax.xml.bind.DatatypeConverter.printHexBinary(compressed));

        BigInteger[] decompressed = decompressPoint(compressed);

        boolean match = decompressed[0].equals(kG[0]) && decompressed[1].equals(kG[1]);
        System.out.println("Decompression matches original? " + match);

        if (!match) {
            System.out.println("Original x: " + kG[0].toString(16));
            System.out.println("Decompressed x: " + decompressed[0].toString(16));
            System.out.println("Original y: " + kG[1].toString(16));
            System.out.println("Decompressed y: " + decompressed[1].toString(16));
        }
    }

//    public static void testModSqrtScalar() {
//        System.out.println("\n--- testModSqrtScalar ---");
//
//        BigInteger a = new BigInteger("12345678901234567890");
//        BigInteger k = a.multiply(a).mod(CURVE_ORDER); // k = a^2 mod n
//
//        BigInteger sqrt = modSqrtScalar(k, CURVE_ORDER);
//        if (sqrt == null) {
//            System.out.println("No sqrt found, but expected one.");
//            return;
//        }
//
//        boolean valid = sqrt.multiply(sqrt).mod(CURVE_ORDER).equals(k);
//        System.out.println("√k = " + sqrt);
//        System.out.println("sqrt^2 mod n == k? " + valid);
//
//        // Show both roots
//        BigInteger altRoot = CURVE_ORDER.subtract(sqrt);
//        System.out.println("Alternative root: " + altRoot);
//        System.out.println("Is altRoot^2 == k? " + altRoot.multiply(altRoot).mod(CURVE_ORDER).equals(k));
//    }

    public static void testModSqrtScalar() {
        System.out.println("\n--- testModSqrtScalar ---");

        BigInteger a = new BigInteger("12345678901234567890");
        BigInteger k = a.multiply(a).mod(P); // k = a^2 mod p

        BigInteger sqrt = modSqrtScalar(k, P);
        if (sqrt == null) {
            System.out.println("No sqrt found, but expected one.");
            return;
        }

        boolean valid = sqrt.multiply(sqrt).mod(P).equals(k);
        System.out.println("√k = " + sqrt);
        System.out.println("sqrt^2 mod p == k? " + valid);

        BigInteger altRoot = P.subtract(sqrt);
        System.out.println("Alternative root: " + altRoot);
        System.out.println("Is altRoot^2 == k? " + altRoot.multiply(altRoot).mod(P).equals(k));
    }

 // Curve parameters (example placeholders)
    static BigInteger N;    // Curve order
    static BigInteger[] G;       // Generator point

    // Precompute tables
    static HashMap<String, BigInteger> squarePrecompute = new HashMap<>();
    static HashMap<String, BigInteger> cubePrecompute = new HashMap<>();

    // Generate square and cube precompute regions for scalars < maxRange
    public static void generateSquareCubePrecompute(int maxRange) {
        for (int k = 1; k < maxRange; k++) {
            BigInteger scalar = BigInteger.valueOf(k);

            // Compute square and cube scalars modulo curve order
            BigInteger squareScalar = scalar.multiply(scalar).mod(N);   // k^2 mod n
            BigInteger cubeScalar = squareScalar.multiply(scalar).mod(N); // k^3 mod n

            // Compute EC points for k^2*G and k^3*G
            BigInteger[] squarePoint = scalarMultiply(squareScalar, GX, GY);
            BigInteger[] cubePoint = scalarMultiply(cubeScalar, GX, GY);

            // Store compressed keys and corresponding k
            squarePrecompute.put(pointToKey(squarePoint), scalar);
            cubePrecompute.put(pointToKey(cubePoint), scalar);
        }
        System.out.println("Square and cube precompute regions generated for range < " + maxRange);
    }


//    public static BigInteger[] pointSubtract(BigInteger[] P, BigInteger[] Q, BigInteger p) {
//        BigInteger[] negQ = pointNegate(Q[0], Q[1], Q[2], p);
//        return pointAddMixed(P, negQ, p); // assuming you have pointAdd implemented in Jacobian
//    }

    // Base point G (secp256k1 generator)
    static final BigInteger GX = new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
    static final BigInteger GY = new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);

    static class TreeNode {
        BigInteger x;          // Affine [x, y]
        BigInteger y;
        TreeNode left;
        TreeNode right;
        int depth;                   // Depth in the recursion tree
        String branchPattern;        // E.g., "L-R-L" (or could use a list)

        // Constructor from affine point with optional metadata
        public TreeNode(BigInteger x, BigInteger y, int depth, String branchPattern) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.branchPattern = branchPattern;
        }
    }

    public static void checkNode(TreeNode node, String label) {
        System.out.println(label + ": scalar = " + node.scalar);
        System.out.println("Point:");
        printPointExpanded(node.x, node.y);

        String scalarStr = node.scalar.toString();

        if (squarePrecompute.containsKey(scalarStr)) {
            System.out.println("Found scalar in squarePrecompute with value: " + squarePrecompute.get(scalarStr));
        } else {
            System.out.println("Scalar not found in squarePrecompute");
        }

        if (cubePrecompute.containsKey(scalarStr)) {
            System.out.println("Found scalar in cubePrecompute with value: " + cubePrecompute.get(scalarStr));
        } else {
            System.out.println("Scalar not found in cubePrecompute");
        }
    }





    // Helper: scalarDivide and wrap into TreeNode
    public static TreeNode scalarDivideToNode(BigInteger divisor, BigInteger[] point) {
        BigInteger[] result = scalarDivide(divisor, point[0], point[1]);
        BigInteger recoveredScalar = null;

        // Try to recover scalar from precomputed maps (if using precomputation)
        String key = pointToKey(result);
        if (squarePrecompute.containsKey(key)) {
            recoveredScalar = squarePrecompute.get(key);
        }
        final BigInteger recoveredScalarFinal = recoveredScalar;
        // Fallback: unknown scalar
        return null; //new TreeNode(BigInteger.ZERO) {{
        //    this.point = result;
        //    this.scalar = recoveredScalarFinal != null ? recoveredScalarFinal : BigInteger.ZERO;
        //}};
    }

    public static TreeNode scalarDivideToNodeExpanded(BigInteger divisor, BigInteger x,
    		BigInteger y) {
        BigInteger[] result = scalarDivide(divisor, x, y);
        BigInteger recoveredScalar = null;

        // Try to recover scalar from precomputed maps (if using precomputation)
        String key = pointToKey(result);
        if (squarePrecompute.containsKey(key)) {
            recoveredScalar = squarePrecompute.get(key);
        }
        final BigInteger recoveredScalarFinal = recoveredScalar;
        // Fallback: unknown scalar
        return new TreeNode(result[0], result[1], 1, ""); //new TreeNode(BigInteger.ZERO) {{
        //    this.point = result;
        //    this.scalar = recoveredScalarFinal != null ? recoveredScalarFinal : BigInteger.ZERO;
        //}};
    }

    public static void buildAndCheckTree(TreeNode node, int depth) {
        if (depth == 0) {
            checkNode(node, "Leaf Node");
            return;
        }

        // Compute (k - 1)G
        BigInteger[] negGJacobian = pointNegate(GX, GY, BigInteger.ONE, P);
        BigInteger[] nodeJacobian = new BigInteger[]{node.x, node.y, BigInteger.ONE};
        BigInteger[] subtract1Jac = pointAddMixed(nodeJacobian[0], nodeJacobian[1], nodeJacobian[2],
                                                  negGJacobian[0], negGJacobian[1]);
        BigInteger[] subtract1Affine = toAffine(subtract1Jac[0], subtract1Jac[1], subtract1Jac[2]);

        // Divide (k-1)G by 2
        TreeNode leftChild = scalarDivideToNodeExpanded(BigInteger.valueOf(2), subtract1Affine[0], subtract1Affine[1]);
        leftChild.depth = depth - 1;
        leftChild.branchPattern = node.branchPattern + "-L";
        checkNode(leftChild, "After (k-1)G / 2");

        // Divide kG by 2 (right child)
        TreeNode rightChild = scalarDivideToNodeExpanded(BigInteger.valueOf(2), node.x, node.y);
        rightChild.depth = depth - 1;
        rightChild.branchPattern = node.branchPattern + "-R";
        checkNode(rightChild, "After kG / 2");

        // Recurse
        buildAndCheckTree(leftChild, depth - 1);
        buildAndCheckTree(rightChild, depth - 1);
    }


    // Main for test
    public static void main(String[] args) {
        // G in Jacobian coords = (x, y, 1)
        BigInteger[] G = new BigInteger[]{GX, GY, BigInteger.ONE};

        // Compute 2G = G + G
        BigInteger[] twoG = pointDouble(G[0], G[1], G[2]);
        BigInteger[] twoG_affine = toAffine(twoG[0], twoG[1], twoG[2]);

        System.out.println("2G (Affine):");
        printPoint(twoG_affine);

        // Compute 3G = 2G + G (mixed addition)
        BigInteger[] threeG = pointAddMixed(twoG[0], twoG[1], twoG[2], GX, GY);
        BigInteger[] threeG_affine = toAffine(threeG[0], threeG[1], threeG[2]);

        System.out.println("\n3G (Affine):");
        printPoint(threeG_affine);

        {
        BigInteger k = BigInteger.valueOf(7);
        BigInteger[] kG = scalarMultiply(k, GX, GY);

        System.out.println("\n7G (Affine):");
        printPoint(kG);
        }
        {
        BigInteger k = new BigInteger("756234554646547657545643645646");
        BigInteger[] kG = scalarMultiply(k, GX, GY);

        System.out.println("\n756234554646547657545643645646G (Affine):");
        printPoint(kG);
        }
        System.out.flush();
        test2(args);
        testPointCompression();
        testModSqrtScalar();

        int numLowBits = 16;
        BigInteger[][] precomputedG = precomputeMultiples(GX, GY,
        		(int) (1L << numLowBits));
        System.out.println("Precomputed multiples of G ready, size: "
        		+ precomputedG.length);

        testRecursiveTreeFromRandomScalar();

    }

    public static void test2(String[] args) {
        // secp256k1 base point G coordinates (hex)
        BigInteger Gx = GX;
        BigInteger Gy = GY;

        BigInteger scalar140 = BigInteger.valueOf(140);
        BigInteger scalar2 = BigInteger.valueOf(2);

        // Calculate 140G
        BigInteger[] point140G = scalarMultiply(scalar140, Gx, Gy);

        // Divide 140G by 2 (i.e., multiply by modular inverse of 2)
        BigInteger[] dividedPoint = scalarDivide(scalar2, point140G[0], point140G[1]);

        // Calculate 70G directly for comparison
        BigInteger scalar70 = BigInteger.valueOf(70);
        BigInteger[] point70G = scalarMultiply(scalar70, Gx, Gy);

        // Check if dividedPoint equals point70G
        if (dividedPoint[0].equals(point70G[0]) && dividedPoint[1].equals(point70G[1])) {
            System.out.println("Test PASSED: (140G) / 2 == 70G");
        } else {
            System.out.println("Test FAILED!");
            System.out.println("Divided point: (" + dividedPoint[0].toString(16) + ", " + dividedPoint[1].toString(16) + ")");
            System.out.println("Expected 70G: (" + point70G[0].toString(16) + ", " + point70G[1].toString(16) + ")");
        }
        test3(args);
    }

    public static void test3(String[] args) {
        BigInteger Gx = new BigInteger(
                "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
            BigInteger Gy = new BigInteger(
                "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);


        // Choose known k > 2^192
        BigInteger k = BigInteger.ONE.shiftLeft(192).add(BigInteger.valueOf(123456789)); // example k > 2^192

        System.out.println("Original k: " + k);

        // Compute P = kG
        BigInteger[] P = scalarMultiply(k, Gx, Gy);

        // Divide P by 2^192 using scalarDivide
        BigInteger twoPower192 = BigInteger.ONE.shiftLeft(192);
        BigInteger[] Q = scalarDivide(twoPower192, P[0], P[1]);

        // Now calculate s = k * 2^(-192) mod CURVE_ORDER
        BigInteger kInv = twoPower192.modInverse(CURVE_ORDER);
        BigInteger s = k.multiply(kInv).mod(CURVE_ORDER);

        System.out.println("Computed scalar s (k * 2^-192 mod n): " + s);

        // Verify that Q == sG
        BigInteger[] sG = scalarMultiply(s, Gx, Gy);

        boolean equal = Q[0].equals(sG[0]) && Q[1].equals(sG[1]);

        System.out.println("Q equals sG? " + equal);

        // Verify recomputing k: s * 2^192 mod n == original k mod n
        BigInteger recomputedK = s.multiply(twoPower192).mod(CURVE_ORDER);
        System.out.println("Recomputed k matches original k? " + recomputedK.equals(k.mod(CURVE_ORDER)));
        test4(args);
    }

    public static void test4(String[] args) {
        // secp256k1 base point G coordinates (hex)
        BigInteger Gx = new BigInteger(
            "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
        BigInteger Gy = new BigInteger(
            "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);

        BigInteger scalar140 = BigInteger.valueOf(140);
        BigInteger scalar2 = BigInteger.valueOf(2);

        // Calculate 141G
        BigInteger[] point140G = scalarMultiply(scalar140, Gx, Gy);

        // Divide 141G by 2 (i.e., multiply by modular inverse of 2)
        BigInteger[] dividedPoint = scalarDivide(scalar2, point140G[0], point140G[1]);

        // Calculate 70G directly for comparison
        BigInteger scalar70 = BigInteger.valueOf(70);
        BigInteger[] point70G = scalarMultiply(scalar70, Gx, Gy);

        // Check if dividedPoint equals point70G
        if (dividedPoint[0].equals(point70G[0]) && dividedPoint[1].equals(point70G[1])) {
            System.out.println("Test PASSED: (140G) / 2 == 70G");
        } else {
            System.out.println("Test FAILED!");
            System.out.println("Divided point: (" + dividedPoint[0].toString(16) + ", " + dividedPoint[1].toString(16) + ")");
            System.out.println("Expected 70G: (" + point70G[0].toString(16) + ", " + point70G[1].toString(16) + ")");
        }
        testSquareCubePrecompute(4096);
    }

    public static void testSquareCubePrecompute(int maxRange) {
        System.out.println("\n--- testSquareCubePrecompute ---");

        // Initialize global curve parameters
        N = CURVE_ORDER;
        G = new BigInteger[]{GX, GY};

        // Generate precompute tables
        generateSquareCubePrecompute(maxRange);

        boolean allPassed = true;

        for (int k = 1; k < maxRange; k++) {
            BigInteger scalar = BigInteger.valueOf(k);

            // Compute square and cube scalars
            BigInteger squareScalar = scalar.multiply(scalar).mod(N);
            BigInteger cubeScalar = squareScalar.multiply(scalar).mod(N);

            // Compute points
            BigInteger[] squarePoint = scalarMultiply(squareScalar, GX, GY);
            BigInteger[] cubePoint = scalarMultiply(cubeScalar, GX, GY);

            // Compress
            String squareKey = pointToKey(squarePoint);
            String cubeKey = pointToKey(cubePoint);

            // Lookup
            BigInteger recoveredFromSquare = squarePrecompute.get(squareKey);
            BigInteger recoveredFromCube = cubePrecompute.get(cubeKey);

            boolean squareOk = scalar.equals(recoveredFromSquare);
            boolean cubeOk = scalar.equals(recoveredFromCube);

            if (!squareOk || !cubeOk) {
                System.out.println("Test FAILED for k = " + k);
                System.out.println("Expected: " + scalar);
                System.out.println("From squareKey: " + squareKey);
                System.out.println("From cubeKey: " + cubeKey);
                System.out.println("From squarePrecompute: " + recoveredFromSquare);
                System.out.println("From cubePrecompute: " + recoveredFromCube);
                allPassed = false;
                break;
            }
        }

        if (allPassed) {
            System.out.println("All square and cube precompute tests PASSED for range < " + maxRange);
        }
        testRecursiveTreeFromRandomScalar();
    }

    public static void testRecursiveTreeFromRandomScalar() {
        System.out.println("\n--- testRecursiveTreeFromRandomScalar ---");

        // 256-bit secure random scalar mod curve order
        SecureRandom rand = new SecureRandom();
        BigInteger k;
        do {
            k = new BigInteger(256, rand).mod(CURVE_ORDER);
        } while (k.signum() == 0); // avoid zero scalar

        System.out.println("Random scalar k = " + k);

        // Compute kG
        BigInteger[] point = scalarMultiply(k, GX, GY);
        System.out.println("kG:");
        printPoint(point);

        // Create tree node
        TreeNode root = new TreeNode(point[0], point[1], 0, "");
        //root.point = point; // Ensure consistency

        // Build and check tree with depth 2 (adjustable)
        buildAndCheckTree(root, 80);
    }


}
