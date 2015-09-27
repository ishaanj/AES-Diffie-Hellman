package cryptography;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class AES {
	
	private static final int KEY_SIZE = 128;

	private static KeyGenerator keygen;
	private static SecretKey secretKey;
	private static Cipher encipher;
	private static Cipher decipher;
	
	static {
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
	
	private static String encryptString(String input) {
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
	
	private static String decryptString(String input) {
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

}

