import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	private static byte[] iv = null;

	// Encrypt string with AES with key passed in
	public static byte[] encrypt(String string, String key) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			AlgorithmParameters params = cipher.getParameters();
			iv = params.getParameterSpec(IvParameterSpec.class).getIV();
			byte[] encrtyptedText = cipher.doFinal(string.getBytes("UTF-8"));
			return encrtyptedText;
		} catch (Exception e) {

		}
		return null;
	}

	public static byte[] getIV() {
		return iv;
	}

	public static void setIV(byte[] IV) {
		iv = IV;
	}

	// Decrypt byte array with key and IV
	public static String decrypt(byte[] string, String key)
			throws InvalidKeyException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			UnsupportedEncodingException, IllegalBlockSizeException,
			BadPaddingException {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
		String encrtyptedText = new String(cipher.doFinal(string), "UTF-8");
		return encrtyptedText;
	}
}
