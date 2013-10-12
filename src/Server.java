import java.math.BigInteger;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import javax.swing.JLabel;

public class Server extends Thread{
	private ServerSocket serverSocket;
	private Boolean running = true;
	OutputStream sockOutput = null;
	public static String nonceFromClient = null;
	public boolean setUpComplete = false;
	public static String nonce = null;
	public static String clientNonce = null;
	public static String sharedKey = null;
	public static byte[] encryptedDHKeyExchange;
	public static String DHKeyExchange;
	
	//private Boolean nonceReceivedFromClient = false;
	public Server( ServerSocket server) {
		
		serverSocket = server;
	}
	
	
	public void handleConnection(InputStream sockInput, OutputStream sockOutput) {
        while(running) {
            byte[] buf=new byte[10000];
            byte[] buf3=new byte[16];
            int bytes_read = 0;
            try {
                // This call to read() will wait forever, until the
                // program on the other side either sends some data,
                // or closes the socket.
                bytes_read = sockInput.read(buf, 0, buf.length);

                // If the socket is closed, sockInput.read() will return -1.
                if(bytes_read < 0) {
                    System.err.println("Tried to read from socket, read() returned < 0,  Closing socket.");
                    //bytes_read = sockInput.read(buf, 0, buf.length);
                    VPNGUI.displayPane.removeAll();
					VPNGUI.displayPane.updateUI();
					VPNGUI.waitingForConnection();
                    return;
                }
                //System.err.println("Received "+bytes_read
                  //                 +" bytes, sending them back to client, data="
                    //               +(new String(buf, 0, bytes_read)));
                VPNGUI.serverRecievedMessage = new String (buf, 0, bytes_read);
                //System.out.println(nonceFromClient);
                if ( VPNGUI.serverRecievedMessage.contains("newConnectionRequestclientNonce:")){
                	VPNGUI.displayPane.removeAll();
					VPNGUI.displayPane.updateUI();
					VPNGUI.displayPane.removeAll();
					VPNGUI.displayPane.updateUI();
					VPNGUI.displayPane.add(new JLabel("Connecting to Client. Please wait..."));
                	nonceFromClient = VPNGUI.serverRecievedMessage.substring(VPNGUI.serverRecievedMessage.indexOf(":")+1);
                	//sendMessage("serverNowSettingUpConnection".getBytes());
                	//nonceReceivedFromClient = true;
                	VPNGUI.serverRecievedMessage = "";
                	String textToEncrypt = "Server,"+nonceFromClient+","+VPNGUI.serverDHKey;
                	//CrytoData data = new
                	byte[] encrypted = AES.encrypt(textToEncrypt, sharedKey);
                	System.out.println(encrypted);
                	System.out.println(encrypted.length);
                	System.out.println(AES.getIV());
                	System.out.println(AES.decrypt(encrypted, sharedKey));
                	//String 
                	System.out.println(nonceFromClient);
                	nonce = ActivitiesPane.generateNonce();
                	sendMessage(nonce.getBytes());
                	System.out.println(nonce);
                	bytes_read = sockInput.read(buf, 0, buf.length);
                	sendMessage(encrypted);
                	bytes_read = sockInput.read(buf, 0, buf.length);
                	sendMessage(AES.getIV());
                	bytes_read = sockInput.read(buf, 0, buf.length);
                	System.out.println(buf);
                	encryptedDHKeyExchange = Client.subArray(buf, 0, bytes_read);
					sendMessage("sendIV".getBytes());
					bytes_read = sockInput.read(buf3, 0, buf3.length);
					AES.setIV(buf3);
					System.out.println(buf3);
					DHKeyExchange = AES.decrypt(encryptedDHKeyExchange, sharedKey);
					System.out.println(DHKeyExchange);
					if (DHKeyExchange.contains("Client") && DHKeyExchange.contains(nonce)){
						VPNGUI.clientDHKey = new BigInteger(DHKeyExchange.split(",")[2]);
						VPNGUI.DHKey = VPNGUI.clientDHKey.pow(VPNGUI.b.intValue()).mod(VPNGUI.p);
						System.out.println(VPNGUI.DHKey);
						VPNGUI.DHKey = new BigInteger(ActivitiesPane.changeKeyTo16Bytes(VPNGUI.DHKey.toString()));
					}
					else{
						new ErrorMessage("Could not authenticate client");
						return;
					}
					sendMessage("showdisplay".getBytes());
                	setUpComplete = true;
                	VPNGUI.serverMode();
                }
                else if (VPNGUI.serverRecievedMessage.contains("client:sendiv")){
                	System.out.println("sending iv");
                	sendMessage(AES.getIV());
                }
                else{
                	System.out.println("before");
                	System.out.println(bytes_read);
                	byte[] encryptedText = Client.subArray(buf, 0, bytes_read);
    				sendMessage("server:sendiv".getBytes());
    				bytes_read = sockInput.read(buf3, 0, buf3.length);
    				AES.setIV(buf3);
    				System.out.println(buf3);
    				String decryptedText = AES.decrypt(encryptedText, VPNGUI.DHKey.toString());
    				System.out.println(decryptedText);
    				VPNGUI.serverRecievedMessage = decryptedText;
    				//VPNGUI.clientRecievedMessage = decryptedText;
    				VPNGUI.displayEncryptedServerReceivedMessage.setText("Encrypted Text from Client: "+encryptedText.toString());
    				VPNGUI.displayServerIV.setText(("IV From Client: "+AES.getIV()).toString());
    				VPNGUI.displayServerReceivedMessage.setText("Decrypted Text: "+ decryptedText);
    				//VPNGUI.serverMode();
          
                
                }//sockOutput.write(buf, 0, bytes_read);
                // This call to flush() is optional - we're saying go
                // ahead and send the data now instead of buffering
                // it.
                //sockOutput.flush();
            }
            catch (Exception e){
                System.err.println("Exception reading from/writing to socket, e="+e);
                e.printStackTrace(System.err);
                return;
            }
        }
    }
	
	public void sendMessage(byte[] message){
		try {
			sockOutput.write(message, 0, message.length);
			sockOutput.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	public void run(){
		System.out.println("Server: waiting for socket");
        InputStream sockInput = null;
        
        while (running) {

            try {
                // This method call, accept(), blocks and waits
                // (forever if necessary) until some other program
                // opens a socket connection to our server.  When some
                // other program opens a connection to our server,
                // accept() creates a new socket to represent that
                // connection and returns.
            	System.out.println(running);
            	ActivitiesPane.serverSock = serverSocket.accept();
                System.err.println("Have accepted new socket.");

                // From this point on, no new socket connections can
                // be made to our server until we call accept() again.

                sockInput = ActivitiesPane.serverSock.getInputStream();
                sockOutput = ActivitiesPane.serverSock.getOutputStream();
                
                sendMessage((VPNGUI.g.toString() + ";" + VPNGUI.p.toString()).getBytes());
            }
            catch (IOException e){
                e.printStackTrace(System.err);
            }

            // Do something with the socket - read bytes from the
            // socket and write them back to the socket until the
            // other side closes the connection.
            handleConnection(sockInput, sockOutput);

            // Now we close the socket.
            try {
                System.err.println("Closing socket.");
                ActivitiesPane.serverSock.close();
            }
            catch (Exception e){
                System.err.println("Exception while closing socket.");
                e.printStackTrace(System.err);
            }

            System.err.println("Finished with socket, waiting for next connection.");
        }
        
        
        
	}
	
	public void shutdown(){
		//running = false;
		nonceFromClient = "";
		setUpComplete = false;
		//nonceReceivedFromClient = false;
	}
}
