package dragon.comm.crypto;

import java.io.UnsupportedEncodingException;
import java.lang.String;import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

public class CryptoUtils {
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String CHARSET_ASCII = "US-ASCII";
    private static final String ALG_SHA1PRNG = "SHA1PRNG";

    public static String encrypt(String msg, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return encrypt(CryptoALG.SHA_1, msg, salt);
    }

    public static String encrypt(CryptoALG alg, String msg, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (alg == null || alg == CryptoALG.None) return msg;
        
        byte[] encrypted = encrypt(alg, msg.getBytes(CHARSET_UTF8), StringUtils.isEmpty(salt) ? null : salt.getBytes(CHARSET_UTF8));
        String hexString = toHexString(encrypted);
        return hexString;
    }

    public static byte[] encrypt(CryptoALG alg, byte[] data, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(alg.getSpecName());
        md.reset();
        if (salt != null) {
            md.update(salt);
        }
        return md.digest(data);
    }

    public static String base64Encode(String s) {
        return new String(Base64.encodeBase64(s.getBytes()), Charset.forName(CHARSET_UTF8));
    }

    public static String base64Decode(String encoded) {
        return new String(Base64.decodeBase64(encoded.getBytes()), Charset.forName(CHARSET_UTF8));
    }

    public static String CreateRandomSalt() throws NoSuchAlgorithmException {
        byte[] asc = new byte[10];

        SecureRandom srand = SecureRandom.getInstance(ALG_SHA1PRNG);
        for (int i = 0; i < asc.length; i ++) {
            int n = srand.nextInt(52);
            if (n < 26) {
                asc[i] = (byte)('A' + n);
            } else {
                asc[i] = (byte)('a' + n - 26);
            }
        }

        return new String(asc, Charset.forName(CHARSET_ASCII));
    }

    public static SecretKey generateKey(String alg) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(alg);
        return generator.generateKey();
    }

    public static SecretKey createSecretKey(byte[] bytes, String alg) {
        return new SecretKeySpec(bytes, alg);
    }

    public static String secretKeyToString(SecretKey key) {
        byte[] encoded = key.getEncoded();
        String algSpec = key.getAlgorithm();
        return algSpec + "#" + toHexString(encoded);
    }

    public static SecretKey createSecretKey(String s) {
        String[] tokens = StringUtils.split(s, '#');
        if (tokens.length == 2) {
            String alg = tokens[0];
            String hex = tokens[1];
            byte[] encoded = hexToBytes(hex);
            return createSecretKey(encoded, alg);
        } else {
            return null;
        }
    }

    public static String encryptPwd(String password, String key) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (!StringUtils.isEmpty(key)) {
            SecretKey secretKey = CryptoUtils.createSecretKey(key);
            return encryptPwd(password, secretKey);
        } else {
            return password;
        }

    }

    public static String encryptPwd(String password, SecretKey secretKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        String encPwd = null;
        if (!StringUtils.isEmpty(password) && secretKey != null) {
            byte[] encoded = new SymmetricCipher(secretKey.getAlgorithm()).encrypt(password, secretKey);
            encPwd = secretKey.getAlgorithm() + "$" + toHexString(encoded);
        }
        if (encPwd == null) {
            encPwd = password;
        }

        return encPwd;
    }

    public static String decryptPwd(String encPwd, String key) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (!StringUtils.isEmpty(key)) {
            SecretKey secretKey = CryptoUtils.createSecretKey(key);
            return decryptPwd(encPwd, secretKey);
        } else {
            return encPwd;
        }
    }

    public static String decryptPwd(String encPwd, SecretKey secretKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        String password = null;
        if (!StringUtils.isEmpty(encPwd) && secretKey != null) {
            int index = encPwd.indexOf('$');
            if (index != -1) {
                String alg = encPwd.substring(0, index);
                String hex = encPwd.substring(index + 1);
                byte[] encoded = hexToBytes(hex);
                password = new SymmetricCipher(alg).decrypt(encoded, secretKey);
            }
        }
        if (password == null) {
            password = encPwd;
        }

        return password;
    }

    static char[] hexChar = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'
    };

    public static String toHexString(byte[] b) {
        char[] hex = new char[b.length << 1];
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xFF;
            // look up high nibble char
            hex[i << 1] = hexChar[v >>> 4];
            // look up low nibble char
            hex[(i << 1) + 1] = hexChar[v & 0x0f];
        }
        return new String(hex);
    }

    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hexidecimal: " + hex);
        }
        byte[] bytes = new byte[hex.length() >> 1];
        for (int i = 0; i < bytes.length; i++) {
            char ch = hex.charAt(i << 1);
            byte b = hexCharToByte(ch);
            b <<= 4;
            ch = hex.charAt((i << 1) + 1);
            b |= hexCharToByte(ch);

            bytes[i] = b;
        }

        return bytes;
    }

    private static byte hexCharToByte(char ch) {
        if (ch >= '0' && ch <= '9') {
            return (byte) (ch - '0');
        } else if (ch >= 'a' && ch <= 'f') {
            return (byte) (ch - 'a' + 10);
        } else if (ch >= 'A' && ch <= 'F') {
            return (byte) (ch - 'A' + 10);
        } else {
            throw new IllegalArgumentException("Invalid hex char: " + ch);
        }
    }
}
