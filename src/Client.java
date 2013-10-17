import java.math.BigInteger;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JLabel;

public class Client extends Thread {

	// Setting up variables
	private Socket sock = null;
	private InputStream sockInput = null;
	private OutputStream sockOutput = null;
	public boolean receivedPG = false;
	public boolean setUPComplete = false;
	public static String nonce;
	public static String sharedKey = null;
	public static String serverNonce;
	public static byte[] encryptedDHKeyExchange;
	public static String DHKeyExchange;

	// Constructor
	public Client(Socket sock) {
		this.sock = sock;
	}

	// Send message to server
	public void sendSomeMessages(byte[] data) {
		try {
			sockOutput = sock.getOutputStream();
		} catch (IOException e) {
			return;
		}
		try {
			sockOutput.write(data, 0, data.length);
		} catch (IOException e) {
		}
	}

	// Get subarray of bytes from a byte array
	public static byte[] subArray(byte[] b, int offset, int length) {
		byte[] sub = new byte[length];
		for (int i = offset; i < offset + length; i++) {
			try {
				sub[i - offset] = b[i];
			} catch (Exception e) {

			}
		}
		return sub;
	}

	// Thread start method
	public void run() {
		int bytes_read = 0;
		byte[] buf = new byte[10000];
		byte[] buf2 = new byte[10000];
		byte[] buf3 = new byte[16];
		byte[] buf4 = new byte[10000];
		try {
			sockInput = sock.getInputStream();
		} catch (IOException e1) {
		}

		// Keep reading data in socket buffer
		while (true) {
			try {
				bytes_read = sockInput.read(buf, 0, buf.length);
				// If authentication is not been completed
				if (receivedPG == false && VPNGUI.g == null) {
					VPNGUI.displayPane.removeAll();
					VPNGUI.displayPane.updateUI();
					VPNGUI.displayPane.add(new JLabel(
							"Connection to Server. Please wait..."));
					String message = new String(buf, 0, bytes_read);
					VPNGUI.g = new BigInteger(message.substring(0,
							message.indexOf(";")));
					VPNGUI.p = new BigInteger(message.substring(
							message.indexOf(";") + 1, message.length()));
					bytes_read = sockInput.read(buf4, 0, buf4.length);
					serverNonce = new String(buf4, 0, bytes_read);
					sendSomeMessages("getnext".getBytes());
					bytes_read = sockInput.read(buf2, 0, buf2.length);
					encryptedDHKeyExchange = subArray(buf2, 0, bytes_read);
					sendSomeMessages("client:sendiv".getBytes());
					bytes_read = sockInput.read(buf3, 0, buf3.length);
					AES.setIV(buf3);
					try {
						DHKeyExchange = AES.decrypt(encryptedDHKeyExchange,
								sharedKey);
					} catch (InvalidKeyException
							| InvalidAlgorithmParameterException
							| NoSuchAlgorithmException | NoSuchPaddingException
							| IllegalBlockSizeException | BadPaddingException e) {
						new ErrorMessage(
								"Please make sure server and client shared keys are the same.");
						return;
					}
					if (DHKeyExchange.contains(nonce)
							&& DHKeyExchange.contains("Server")) {
						VPNGUI.serverDHKey = new BigInteger(
								DHKeyExchange.split(",")[2]);
						VPNGUI.a = BigInteger.probablePrime(16,
								new SecureRandom());
						VPNGUI.clientDHKey = VPNGUI.g.pow(VPNGUI.a.intValue())
								.mod(VPNGUI.p);
						VPNGUI.DHKey = VPNGUI.serverDHKey.pow(
								VPNGUI.a.intValue()).mod(VPNGUI.p);
						VPNGUI.DHKey = new BigInteger(
								ActivitiesPane.changeKeyTo16Bytes(VPNGUI.DHKey
										.toString()));
						String textToEncrypt = "Client," + serverNonce + ","
								+ VPNGUI.clientDHKey;
						byte[] encrypted = AES
								.encrypt(textToEncrypt, sharedKey);
						sendSomeMessages(encrypted);
						bytes_read = sockInput.read(buf, 0, buf.length);
						sendSomeMessages(AES.getIV());
						bytes_read = sockInput.read(buf, 0, buf.length);
						VPNGUI.clientMode();
					} else {
						new ErrorMessage("Server not authenticated");
						return;
					}
				}
				// If authentication has been completeed and server is asking
				// for IV used
				// for decryption
				else if ((bytes_read > 0)
						&& (new String(buf, 0, bytes_read))
								.contains("server:sendiv")) {
					sendSomeMessages(AES.getIV());
				}
				// Else data received is a message
				else if (bytes_read > 0) {
					byte[] encryptedText = subArray(buf, 0, bytes_read);
					sendSomeMessages("client:sendiv".getBytes());
					bytes_read = sockInput.read(buf3, 0, buf3.length);
					AES.setIV(buf3);
					String decryptedText;
					try {
						decryptedText = AES.decrypt(encryptedText,
								VPNGUI.DHKey.toString());
						VPNGUI.clientRecievedMessage = decryptedText;
						VPNGUI.displayEncrypyedClientReceivedMessage
								.setText("Encrypted Text from Server: "
										+ encryptedText.toString());
						VPNGUI.displayClientIV
								.setText(("IV From Server: " + AES.getIV())
										.toString());
						VPNGUI.displayClientReceivedMessage
								.setText("Decrypted Text: " + decryptedText);
					} catch (InvalidKeyException
							| InvalidAlgorithmParameterException
							| NoSuchAlgorithmException | NoSuchPaddingException
							| IllegalBlockSizeException | BadPaddingException e) {
						new ErrorMessage("Error decrypting data");
						return;
					}
				}
			} catch (IOException e) {
				new ErrorMessage("Oops this is embarassing, please try again.");
			}
		}
	}

	public void shutdown() {

	}
}
