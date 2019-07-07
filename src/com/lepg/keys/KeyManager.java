package com.lepg.keys;

import javax.crypto.Cipher;
import java.security.*;

public class KeyManager {
    private String algorithm;
    private String provider;

    public KeyManager(String algorithm, String provider) {
        this.algorithm = algorithm;
        this.provider = provider;
    }

    public KeyPair genKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(this.algorithm/*, this.provider*/);
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG"/*, this.provider*/);
        keyGen.initialize(1024, random);
        return keyGen.generateKeyPair();
    }

    public byte[] decryptData(byte[] encryptedData, PrivateKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(this.algorithm/*, this.provider*/);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    public byte[] encryptData(byte[] data, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(this.algorithm/*, this.provider*/);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
