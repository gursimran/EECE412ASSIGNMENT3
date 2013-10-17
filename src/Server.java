import java.math.BigInteger;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import javax.swing.JLabel;

public class Server extends Thread {

	// Setting up variables
	private ServerSocket serverSocket;
	private Boolean running = true;
	OutputStream sockOutput = null;
	public static String nonceFromClient = null;
	public static String nonce = null;
	public static String clientNonce = null;
	public static String sharedKey = null;
	public static byte[] encryptedDHKeyExchange;
	public static String DHKeyExchange;

	// Constructor
	public Server(ServerSocket server) {
		serverSocket = server;
	}

	// Method that reads data from socket buffer (remians in here till client
	// disconnects)
	public void handleConnection(InputStream sockInput, OutputStream sockOutput) {
		while (running) {
			byte[] buf = new byte[10000];
			byte[] buf3 = new byte[16];
			int bytes_read = 0;
			try {
				bytes_read = sockInput.read(buf, 0, buf.length);

				// If the socket is closed, sockInput.read() will return -1.
				if (bytes_read < 0) {
					VPNGUI.displayPane.removeAll();
					VPNGUI.displayPane.updateUI();
					VPNGUI.waitingForConnection();
					return;
				}

				VPNGUI.serverRecievedMessage = new String(buf, 0, bytes_read);

				// Client asking for setting up mutual authentication
				if (VPNGUI.serverRecievedMessage
						.contains("newConnectionRequestclientNonce:")) {
					VPNGUI.displayPane.removeAll();
					VPNGUI.displayPane.updateUI();
					VPNGUI.displayPane.removeAll();
					VPNGUI.displayPane.updateUI();
					VPNGUI.displayPane.add(new JLabel(
							"Connecting to Client. Please wait..."));
					nonceFromClient = VPNGUI.serverRecievedMessage
							.substring(VPNGUI.serverRecievedMessage
									.indexOf(":") + 1);
					VPNGUI.serverRecievedMessage = "";
					String textToEncrypt = "Server," + nonceFromClient + ","
							+ VPNGUI.serverDHKey;
					byte[] encrypted = AES.encrypt(textToEncrypt, sharedKey);
					nonce = ActivitiesPane.generateNonce();
					sendMessage(nonce.getBytes());
					bytes_read = sockInput.read(buf, 0, buf.length);
					sendMessage(encrypted);
					bytes_read = sockInput.read(buf, 0, buf.length);
					sendMessage(AES.getIV());
					bytes_read = sockInput.read(buf, 0, buf.length);
					encryptedDHKeyExchange = Client
							.subArray(buf, 0, bytes_read);
					sendMessage("sendIV".getBytes());
					bytes_read = sockInput.read(buf3, 0, buf3.length);
					AES.setIV(buf3);
					DHKeyExchange = AES.decrypt(encryptedDHKeyExchange,
							sharedKey);
					if (DHKeyExchange.contains("Client")
							&& DHKeyExchange.contains(nonce)) {
						VPNGUI.clientDHKey = new BigInteger(
								DHKeyExchange.split(",")[2]);
						VPNGUI.DHKey = VPNGUI.clientDHKey.pow(
								VPNGUI.b.intValue()).mod(VPNGUI.p);
						VPNGUI.DHKey = new BigInteger(
								ActivitiesPane.changeKeyTo16Bytes(VPNGUI.DHKey
										.toString()));
						VPNGUI.serverMode();
					} else {
						new ErrorMessage("Could not authenticate client");
						return;
					}
					sendMessage("showdisplay".getBytes());
				} else if (VPNGUI.serverRecievedMessage
						.contains("client:sendiv")) {
					sendMessage(AES.getIV());
				} else {
					byte[] encryptedText = Client.subArray(buf, 0, bytes_read);
					sendMessage("server:sendiv".getBytes());
					bytes_read = sockInput.read(buf3, 0, buf3.length);
					AES.setIV(buf3);
					String decryptedText = AES.decrypt(encryptedText,
							VPNGUI.DHKey.toString());
					VPNGUI.serverRecievedMessage = decryptedText;
					VPNGUI.displayEncryptedServerReceivedMessage
							.setText("Encrypted Text from Client: "
									+ encryptedText.toString());
					VPNGUI.displayServerIV.setText(("IV From Client: " + AES
							.getIV()).toString());
					VPNGUI.displayServerReceivedMessage
							.setText("Decrypted Text: " + decryptedText);
				}
			} catch (Exception e) {
				new ErrorMessage("Oops, please try again.");
				return;
			}
		}
	}

	// Send message to client
	public void sendMessage(byte[] message) {
		try {
			sockOutput.write(message, 0, message.length);
			sockOutput.flush();
		} catch (IOException e) {
		}
	}

	// Server start thread method
	public void run() {

		InputStream sockInput = null;
		// Remain in this while loop and wait for connection while server is
		// still listening
		while (running) {
			try {
				// This method call, accept(), blocks and waits
				// (forever if necessary) until some other program
				// opens a socket connection to our server. When some
				// other program opens a connection to our server,
				// accept() creates a new socket to represent that
				// connection and returns.
				ActivitiesPane.serverSock = serverSocket.accept();
				// From this point on, no new socket connections can
				// be made to our server until we call accept() again.
				sockInput = ActivitiesPane.serverSock.getInputStream();
				sockOutput = ActivitiesPane.serverSock.getOutputStream();
				sendMessage((VPNGUI.g.toString() + ";" + VPNGUI.p.toString())
						.getBytes());
			} catch (IOException e) {
			}

			// Do something with the socket - read bytes from the
			// socket and write them back to the socket until the
			// other side closes the connection.
			handleConnection(sockInput, sockOutput);

			// Now we close the socket.
			try {
				ActivitiesPane.serverSock.close();
			} catch (Exception e) {
			}
		}

	}

	public void shutdown() {
		nonceFromClient = "";
	}
}
