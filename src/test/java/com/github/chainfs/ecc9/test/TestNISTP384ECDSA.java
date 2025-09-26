package com.github.chainfs.ecc9.test;
import java.math.BigInteger;
import java.security.MessageDigest;

import com.github.chainfs.ecc9.NISTP384ECDSA;

public class TestNISTP384ECDSA {

    public static void main(String[] args) throws Exception {
        testKeyGeneration();
        testSignAndVerify();
        testVerifyWithModifiedMessage();
        testVerifyWithModifiedSignature();
        System.out.println("All tests passed!");
    }

    private static void testKeyGeneration() {
        NISTP384ECDSA ecdsa = new NISTP384ECDSA();

        BigInteger priv = ecdsa.getPrivateKey();
        BigInteger pubX = ecdsa.getPublicKeyX();
        BigInteger pubY = ecdsa.getPublicKeyY();

        // Private key should be in [1, n-1]
        assert priv.compareTo(BigInteger.ONE) >= 0 : "Private key too small";
        assert priv.compareTo(NISTP384ECDSA.n) < 0 : "Private key too large";

        // Public key coordinates should be valid points on the curve (very basic check)
        assert pubX != null && pubY != null : "Public key point is null";
    }

    private static void testSignAndVerify() throws Exception {
        NISTP384ECDSA ecdsa = new NISTP384ECDSA();

        String message = "Test message for signing";
        BigInteger hash = hashMessage(message);

        NISTP384ECDSA.Signature sig = ecdsa.sign(hash);
        assert sig.r != null && sig.s != null : "Signature components null";

        boolean valid = ecdsa.verify(hash, sig);
        assert valid : "Signature should be valid";
    }

    private static void testVerifyWithModifiedMessage() throws Exception {
        NISTP384ECDSA ecdsa = new NISTP384ECDSA();

        String message = "Test message for signing";
        BigInteger hash = hashMessage(message);
        NISTP384ECDSA.Signature sig = ecdsa.sign(hash);

        String tamperedMessage = "Tampered message";
        BigInteger tamperedHash = hashMessage(tamperedMessage);

        boolean valid = ecdsa.verify(tamperedHash, sig);
        assert !valid : "Signature verification should fail on modified message";
    }

    private static void testVerifyWithModifiedSignature() throws Exception {
        NISTP384ECDSA ecdsa = new NISTP384ECDSA();

        String message = "Test message for signing";
        BigInteger hash = hashMessage(message);
        NISTP384ECDSA.Signature sig = ecdsa.sign(hash);

        // Modify signature s slightly
        NISTP384ECDSA.Signature badSig = new NISTP384ECDSA.Signature(sig.r, sig.s.add(BigInteger.ONE));

        boolean valid = ecdsa.verify(hash, badSig);
        assert !valid : "Signature verification should fail on modified signature";
    }

    private static BigInteger hashMessage(String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        byte[] hashBytes = digest.digest(message.getBytes("UTF-8"));
        return new BigInteger(1, hashBytes);
    }
}
