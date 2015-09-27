package cryptography;

import java.io.UnsupportedEncodingException;
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

	public String encryptString(String input) {
		String output = "";

		try {
			byte hold[] = input.getBytes("UTF8");
			byte encryptedBytes[] = encipher.doFinal(hold);
			output = Base64.getEncoder().encodeToString(encryptedBytes);

		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return output;
	}

	public String decryptString(String input) {
		String output = "";

		byte decryptedBytes[];
		try {
			byte hold[] = Base64.getDecoder().decode(input);
			decryptedBytes = decipher.doFinal(hold);
			output = new String(decryptedBytes);

		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} 
		return output;
	}

	public String getKey() {
		return Arrays.toString(secretKey.getEncoded());
	}

}

