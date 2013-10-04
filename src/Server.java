import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Server extends Thread{
	private ServerSocket serverSocket;
	private Boolean running = true;
	OutputStream sockOutput = null;
	public Server( ServerSocket server) {
		
		serverSocket = server;
	}
	
	
	public void handleConnection(InputStream sockInput, OutputStream sockOutput) {
        while(running) {
            byte[] buf=new byte[1024];
            int bytes_read = 0;
            try {
                // This call to read() will wait forever, until the
                // program on the other side either sends some data,
                // or closes the socket.
                bytes_read = sockInput.read(buf, 0, buf.length);

                // If the socket is closed, sockInput.read() will return -1.
                if(bytes_read < 0) {
                    System.err.println("Tried to read from socket, read() returned < 0,  Closing socket.");
                    return;
                }
                //System.err.println("Received "+bytes_read
                  //                 +" bytes, sending them back to client, data="
                    //               +(new String(buf, 0, bytes_read)));
                VPNGUI.serverRecievedMessage = new String (buf, 0, bytes_read);
                VPNGUI.serverMode();
                //sockOutput.write(buf, 0, bytes_read);
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
	
	public void sendMessage(String message){
		try {
			sockOutput.write(message.getBytes(), 0, message.length());
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
		running = false;
	}
}
