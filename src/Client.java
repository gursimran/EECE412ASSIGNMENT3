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
	// private String serverHostname = null;
	// private int serverPort = 0;
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
	public Client(Socket sock) {
		this.sock = sock;
	}

	public void sendSomeMessages(int iterations, byte[] data) {
		// System.err.println("Opening connection to "+serverHostname+" port "+serverPort);
		try {
			System.out.println("sending");
			sockOutput = sock.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return;
		}

		// System.err.println("About to start reading/writing to/from socket.");

		int bytes_read = 0;
		for (int loopi = 1; loopi <= iterations; loopi++) {
			try {
				sockOutput.write(data, 0, data.length);
				// bytes_read = sockInput.read(buf, 0, buf.length);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			if (bytes_read < data.length) {
				// System.err.println("run: Sent "+data.length+" bytes, server should have sent them back, read "+bytes_read+" bytes, not the same number of bytes.");
			} else {
				// System.err.println("Sent "+bytes_read+" bytes to server and received them back again, msg = "+(new
				// String(data)));
			}

			// Sleep for a bit so the action doesn't happen to fast - this is
			// purely for reasons of demonstration, and not required
			// technically.
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
			;
		}
		// System.err.println("Done reading/writing to/from socket, closing socket.");

		// System.err.println("Exiting.");
	}

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
	public void run() {
		int bytes_read = 0;
		byte[] buf = new byte[10000];
		byte[] buf2 = new byte[10000];
		byte[] buf3 = new byte[16];
		byte[] buf4 = new byte[10000];
		try {
			sockInput = sock.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true) {
			try {
				bytes_read = sockInput.read(buf, 0, buf.length);
				
				if (receivedPG == false && VPNGUI.g == null){
					VPNGUI.displayPane.removeAll();
					VPNGUI.displayPane.updateUI();
					
					VPNGUI.displayPane.add(new JLabel("Connection to Server. PLease wait..."));
					String message = new String(buf, 0, bytes_read);
					VPNGUI.g = new BigInteger(message.substring(0, message.indexOf(";")));
					VPNGUI.p = new BigInteger(message.substring(message.indexOf(";")+1, message.length()));
					System.out.println("client p:" + VPNGUI.p);
					System.out.println("client g:" + VPNGUI.g);
					bytes_read = sockInput.read(buf4, 0, buf4.length);
					serverNonce = new String(buf4, 0, bytes_read);
					sendSomeMessages(1, "getnext".getBytes());
					bytes_read = sockInput.read(buf2, 0, buf2.length);
					System.out.println(buf2);
					encryptedDHKeyExchange = subArray(buf2, 0, bytes_read);
					sendSomeMessages(1, "client:sendiv".getBytes());
					bytes_read = sockInput.read(buf3, 0, buf3.length);
					AES.setIV(buf3);
					System.out.println(buf3);
					try {
						DHKeyExchange = AES.decrypt(encryptedDHKeyExchange, sharedKey);
					} catch (InvalidKeyException
							| InvalidAlgorithmParameterException
							| NoSuchAlgorithmException | NoSuchPaddingException
							| IllegalBlockSizeException | BadPaddingException e) {
						// TODO Auto-generated catch block
						
						new ErrorMessage("Please make sure server and client shared keys are the same.");
						return;
					}
					System.out.println(DHKeyExchange);
					if(DHKeyExchange.contains(nonce) && DHKeyExchange.contains("Server")){
						VPNGUI.serverDHKey = new BigInteger(DHKeyExchange.split(",")[2]);
						System.out.println(VPNGUI.serverDHKey);
						VPNGUI.a = BigInteger.probablePrime(16, new SecureRandom());
						VPNGUI.clientDHKey = VPNGUI.g.pow(VPNGUI.a.intValue()).mod(VPNGUI.p);
						System.out.println(VPNGUI.clientDHKey);
						VPNGUI.DHKey = VPNGUI.serverDHKey.pow(VPNGUI.a.intValue()).mod(VPNGUI.p);
						System.out.println(VPNGUI.DHKey);
						VPNGUI.DHKey = new BigInteger(ActivitiesPane.changeKeyTo16Bytes(VPNGUI.DHKey.toString()));
						String textToEncrypt = "Client," + serverNonce + "," + VPNGUI.clientDHKey;
						byte[] encrypted = AES.encrypt(textToEncrypt, sharedKey);
						System.out.println(encrypted);
	                	System.out.println(encrypted.length);
	                	System.out.println(AES.getIV());
	                	try {
							System.out.println(AES.decrypt(encrypted, sharedKey));
						} catch (InvalidKeyException
								| InvalidAlgorithmParameterException
								| NoSuchAlgorithmException
								| NoSuchPaddingException
								| IllegalBlockSizeException
								| BadPaddingException e) {
							// TODO Auto-generated catch block
							new ErrorMessage("Error decrypting data");
							return;
						}
	                	System.out.println(serverNonce);
	                	sendSomeMessages(1, encrypted);
	                	bytes_read = sockInput.read(buf, 0, buf.length);
	                	sendSomeMessages(1,AES.getIV());
						//sendSomeMessages(iterations, data)
						//b = BigInteger.probablePrime(16, rnd);
						//BigInteger a = new BigInteger("0");
						//serverDHKey = g.pow(b.intValue()).mod(p);
	                	bytes_read = sockInput.read(buf, 0, buf.length);
						VPNGUI.showCLient = true;
	                	///VPNGUI.clientMode();VPNGUI.clientMode();VPNGUI.clientMode();VPNGUI.clientMode();VPNGUI.clientMode();
						//return;
					}else{
						new ErrorMessage("Server not authenticated");
						return;
					}
					
					//receivedPG = true;
				}else if((new String(buf, 0, bytes_read)).contains("server:sendiv")){
					System.out.println("sending iv");
                	sendSomeMessages(1,AES.getIV());
				}
				else{
				System.out.println("before");
				byte[] encryptedText = subArray(buf, 0, bytes_read);
				sendSomeMessages(1, "client:sendiv".getBytes());
				bytes_read = sockInput.read(buf3, 0, buf3.length);
				AES.setIV(buf3);
				System.out.println(buf3);
				String decryptedText;
				try {
					decryptedText = AES.decrypt(encryptedText, VPNGUI.DHKey.toString());
					System.out.println(decryptedText);
					VPNGUI.clientRecievedMessage = decryptedText;
					VPNGUI.displayEncrypyedClientReceivedMessage.setText("Encrypted Text from Server: "+encryptedText.toString());
					VPNGUI.displayClientIV.setText(("IV From Server: "+AES.getIV()).toString());
					VPNGUI.displayClientReceivedMessage.setText("Decrypted Text: "+ decryptedText);
				} catch (InvalidKeyException
						| InvalidAlgorithmParameterException
						| NoSuchAlgorithmException | NoSuchPaddingException
						| IllegalBlockSizeException | BadPaddingException e) {
					// TODO Auto-generated catch block
					new ErrorMessage("Error decrypting data");
					return;
				}
				
				//VPNGUI.clientMode();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				new ErrorMessage("Oops this is embarassing, please try again.");
				
			}
		}
	}
	
	public void shutdown(){
		
	}
}
