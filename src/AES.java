import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


public class AES {

	private static byte[] iv = null;
	
	public static byte[] encrypt (String string, String key){
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			AlgorithmParameters params = cipher.getParameters();
			iv = params.getParameterSpec(IvParameterSpec.class).getIV();
			byte[] encrtyptedText = cipher.doFinal(string.getBytes("UTF-8"));
			return encrtyptedText;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidParameterSpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	
	public static byte[] getIV(){
		return iv;
	}
	
	public static void setIV(byte[] IV){
		iv = IV;
	}
	public static String decrypt(byte[] string, String key){
		
		
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			String encrtyptedText = new String(cipher.doFinal(string), "UTF-8");
			return encrtyptedText;
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
