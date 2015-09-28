package cryptography;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	private final int KEY_SIZE = 128;

	private KeyGenerator keygen;
	private SecretKey secretKey;
	private Cipher encipher;
	private Cipher decipher;

	/**
	 * Use in Server to create the first random instance of AES
	 */
	public AES() {
		try {
			keygen = KeyGenerator.getInstance("AES");
			keygen.init(KEY_SIZE);
			secretKey = keygen.generateKey();

			encipher = Cipher.getInstance("AES");
			encipher.init(Cipher.ENCRYPT_MODE, secretKey);

			decipher = Cipher.getInstance("AES");
			decipher.init(Cipher.DECRYPT_MODE, secretKey);

		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not load AES Algorithm");
			System.exit(0);
		} catch (NoSuchPaddingException e) {
			System.out.println("Could not load padding PKCS5");
			System.exit(0);
		} catch (InvalidKeyException e) {
			System.out.println("Key is considered Invalid");
			System.exit(0);
		}
	}
	
	/**
	 * Use in Client. Creates a copy of Server AES. Use static method {@link AES#stringToKey(String)} to get the byte array from the ip keystring
	 * @param key
	 */
	public AES(byte key[]) {
		try {
			secretKey = new SecretKeySpec(key, "AES");

			encipher = Cipher.getInstance("AES");
			encipher.init(Cipher.ENCRYPT_MODE, secretKey);

			decipher = Cipher.getInstance("AES");
			decipher.init(Cipher.DECRYPT_MODE, secretKey);

		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not load AES Algorithm");
			System.exit(0);
		} catch (NoSuchPaddingException e) {
			System.out.println("Could not load padding PKCS5");
			System.exit(0);
		} catch (InvalidKeyException e) {
			System.out.println("Key is considered Invalid");
			System.exit(0);
		}
	}

	public byte[] encryptBytes(byte input[]) {
		byte encryptedBytes[] = null;
		try {
			encryptedBytes = encipher.doFinal(input);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return encryptedBytes;
	}

	public byte[] decryptBytes(byte input[]) {
		byte decryptedBytes[] = null;
		try {
			decryptedBytes = decipher.doFinal(input);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} 
		return decryptedBytes;
	}

	/**
	 * Use in Server to obtain the Key in string format and send it via outputstream to client
	 * @return Base64 encoded string of the key
	 */
	public String keyToString() {
		byte key[] = secretKey.getEncoded();
		return Base64.getEncoder().encodeToString(key);
	}
	
	/**
	 * Call static method in Client to parse the string key from I/PStream and generate the key byte[]
	 * @param key
	 * @return byte[] format of decrypted Key
	 */
	public static byte[] stringToKey(String key) {
		return Base64.getDecoder().decode(key);
	}

}

