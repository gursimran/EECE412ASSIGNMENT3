import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Client extends Thread {
	//private String serverHostname = null;
    //private int serverPort = 0;
    private Socket sock = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;

    public Client(Socket sock){
        this.sock = sock;
    }
    
    
    public void sendSomeMessages(int iterations, byte[] data) {
        //System.err.println("Opening connection to "+serverHostname+" port "+serverPort);
        try {
           
            sockOutput = sock.getOutputStream();
        }
        catch (IOException e){
            e.printStackTrace(System.err);
            return;
        }

        System.err.println("About to start reading/writing to/from socket.");

        byte[] buf = new byte[data.length];
        int bytes_read = 0;
        for(int loopi = 1; loopi <= iterations; loopi++) {
            try {
                sockOutput.write(data, 0, data.length); 
                //bytes_read = sockInput.read(buf, 0, buf.length);
            }
            catch (IOException e){
                e.printStackTrace(System.err);
            }
            if(bytes_read < data.length) {
                System.err.println("run: Sent "+data.length+" bytes, server should have sent them back, read "+bytes_read+" bytes, not the same number of bytes.");
            }
            else {
                System.err.println("Sent "+bytes_read+" bytes to server and received them back again, msg = "+(new String(data)));
            }

            // Sleep for a bit so the action doesn't happen to fast - this is purely for reasons of demonstration, and not required technically.
            try { Thread.sleep(50);} catch (Exception e) {}; 
        }
        System.err.println("Done reading/writing to/from socket, closing socket.");

 
        System.err.println("Exiting.");
    }
    
    public void run(){
    	int bytes_read = 0;
    	 byte[] buf = new byte[1024];

		 try {
			sockInput = sock.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	while(true){
    	 try {
			bytes_read = sockInput.read(buf, 0, buf.length);
			VPNGUI.clientRecievedMessage = new String(buf, 0, bytes_read);
			VPNGUI.clientMode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	}
    }
}
