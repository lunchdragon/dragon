package dragon.comm.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SymmetricCipher {

    private Cipher cipher;

    public SymmetricCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this(CryptoALG.AES);
    }

    public SymmetricCipher(CryptoALG alg) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this(alg.getSpecName());
    }

    public SymmetricCipher(String algSpec) throws NoSuchAlgorithmException, NoSuchPaddingException {
        cipher = Cipher.getInstance(algSpec);
    }

    public byte[] encrypt(String input, SecretKey key)
            throws InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            NoSuchPaddingException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] inputBytes = input.getBytes();
        return cipher.doFinal(inputBytes);
    }

    public String decrypt(byte[] encryptionBytes, SecretKey key)
            throws InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException,
            NoSuchAlgorithmException,
            NoSuchPaddingException {
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] recoveredBytes =
                cipher.doFinal(encryptionBytes);
        String recovered =
                new String(recoveredBytes);
        return recovered;
    }
}
