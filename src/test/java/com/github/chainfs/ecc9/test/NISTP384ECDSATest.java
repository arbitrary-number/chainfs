package com.github.chainfs.ecc9.test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.chainfs.ecc9.NISTP384ECDSA;

public class NISTP384ECDSATest {

    private NISTP384ECDSA ecdsa;

    @BeforeEach
    public void setUp() {
        ecdsa = new NISTP384ECDSA();
    }

    @Test
    public void testKeyGeneration() {
        BigInteger priv = ecdsa.getPrivateKey();
        BigInteger pubX = ecdsa.getPublicKeyX();
        BigInteger pubY = ecdsa.getPublicKeyY();

        assertNotNull(priv, "Private key should not be null");
        assertNotNull(pubX, "Public key X coordinate should not be null");
        assertNotNull(pubY, "Public key Y coordinate should not be null");

        assertTrue(priv.compareTo(BigInteger.ONE) >= 0, "Private key too small");
        assertTrue(priv.compareTo(NISTP384ECDSA.n) < 0, "Private key too large");
    }

    @Test
    public void testSignAndVerify() throws Exception {
        String message = "Test message for signing";
        BigInteger hash = hashMessage(message);

        NISTP384ECDSA.Signature sig = ecdsa.sign(hash);
        assertNotNull(sig);
        assertNotNull(sig.r);
        assertNotNull(sig.s);

        boolean valid = ecdsa.verify(hash, sig);
        assertTrue(valid, "Signature should be valid");
    }

    @Test
    public void testVerifyWithModifiedMessage() throws Exception {
        String message = "Test message for signing";
        BigInteger hash = hashMessage(message);
        NISTP384ECDSA.Signature sig = ecdsa.sign(hash);

        String tamperedMessage = "Tampered message";
        BigInteger tamperedHash = hashMessage(tamperedMessage);

        boolean valid = ecdsa.verify(tamperedHash, sig);
        assertFalse(valid, "Signature verification should fail on modified message");
    }

    @Test
    public void testVerifyWithModifiedSignature() throws Exception {
        String message = "Test message for signing";
        BigInteger hash = hashMessage(message);
        NISTP384ECDSA.Signature sig = ecdsa.sign(hash);

        // Modify signature s slightly
        NISTP384ECDSA.Signature badSig = new NISTP384ECDSA.Signature(sig.r, sig.s.add(BigInteger.ONE));

        boolean valid = ecdsa.verify(hash, badSig);
        assertFalse(valid, "Signature verification should fail on modified signature");
    }

    private BigInteger hashMessage(String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        byte[] hashBytes = digest.digest(message.getBytes("UTF-8"));
        return new BigInteger(1, hashBytes);
    }
}
