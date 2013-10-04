import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ActivitiesPane extends JPanel {
	public static Thread serverThread = null;
	public static Socket serverSock = null;
	private ServerSocket serverSocket = null;
	public static Socket clientSocket = null;
	public Thread clientThread = null;
	
	public ActivitiesPane() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public void display(String connectionType) {

		Font font = new Font("Arial", Font.BOLD, 16);
		JTextArea title = new JTextArea("Settings");
		title.setFont(font);
		title.setEditable(false);
		title.setMaximumSize(new Dimension(700, 30));
		this.add(title);

		if (connectionType == "Client") {
			JLabel ipAddressLabel = new JLabel("IP Address");
			final JTextField ipAddress = new JTextField();
			ipAddress.setMaximumSize(new Dimension(300, 20));
			JLabel portNumberLabel = new JLabel("Port");
			final JTextField portNumber = new JTextField();
			portNumber.setMaximumSize(new Dimension(300, 20));
			JLabel emptySpace = new JLabel("                  ");
			final JButton connect = new JButton("Connect");


			connect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String ipAddressValue = ipAddress.getText();
					String portNumberValue = portNumber.getText();
					Boolean validIP = true;
					Boolean validPort = true;
					if (connect.getText().equalsIgnoreCase("Connect")){
						if (!ipAddressValue.isEmpty()){
							int countPeriods = 0;
							int countBetweenPeriods = 0;
							for (char c : ipAddressValue.toCharArray()){
								if(c != '.'){
									if(!Character.isDigit(c)){
										new ErrorMessage("Please Make sure IP Address is in valid form: xxx.xxx.xxx.xxx");
										validIP = false;
										break;
									}else{
										countBetweenPeriods++;
									}
									if (countBetweenPeriods > 3){
										new ErrorMessage("Please Make sure IP Address is in valid form: xxx.xxx.xxx.xxx");
										validIP = false;
										break;
									}
								}
								else{
									countPeriods++;
									countBetweenPeriods = 0;
								}
							}
							if (countPeriods != 3){
								new ErrorMessage("Please Make sure IP Address is in valid form: xxx.xxx.xxx.xxx");
								validIP = false;
							}
						}
						else{
							new ErrorMessage("Please Make sure IP Address is in valid form: xxx.xxx.xxx.xxx");
							validIP = false;
						}

						if (validIP){
							try{
								int port = Integer.parseInt(portNumberValue);
								if (port < 1024 || port > 65535 ){
									new ErrorMessage("Please Make sure port number is between 1025 and 65535");
									validPort = false;
								}
							}catch (Exception e2){
								new ErrorMessage("Please Make sure port number entered is a number");
								validPort = false;
							}
							if (validPort){
								try {
									clientSocket = new Socket(ipAddressValue, Integer.parseInt(portNumberValue));
									clientThread = new Client(clientSocket);
									clientThread.start();
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									new ErrorMessage("Server not available, Please make sure the IP address and port are correct");
								}
								connect.setText("Disconnect");
								VPNGUI.startVPN("Client");
							}
						}
					}else{
						connect.setText("Connect");
						try {
							clientThread.stop();
							clientSocket.close();
						} catch (IOException e1) {}
					}
				}

			});

			this.add(ipAddressLabel);
			this.add(ipAddress);
			this.add(portNumberLabel);
			this.add(portNumber);
			this.add(emptySpace);
			this.add(connect);

		}

		if (connectionType == "Server") {

			JLabel portNumberLabel = new JLabel("Port");
			final JTextField portNumber = new JTextField();
			portNumber.setMaximumSize(new Dimension(300, 20));
			JLabel emptySpace = new JLabel("                  ");
			final JButton startServer = new JButton("Start Server");

			this.add(portNumberLabel);
			this.add(portNumber);
			this.add(emptySpace);
			this.add(startServer);

			startServer.addActionListener(new ActionListener() {
				@SuppressWarnings({ "deprecation"})
				public void actionPerformed(ActionEvent e) {
					String portNumberValue = portNumber.getText();
					Boolean validPort = true;
					int portNumber = 0;
					if (startServer.getText().equalsIgnoreCase("Start Server")){
						try{
							int port = Integer.parseInt(portNumberValue);
							if (port < 1024 || port > 65535 ){
								new ErrorMessage("Please Make sure port number is between 1025 and 65535");
								validPort = false;
							}
							portNumber = port;
						}catch (Exception e2){
							new ErrorMessage("Please Make sure port number entered is a number");
							validPort = false;
						}
						if (validPort){

							try {
								serverSocket = new ServerSocket(portNumber);
								serverThread = new Server(serverSocket);
								serverThread.start();
								startServer.setText("Stop Server");
								VPNGUI.startVPN("Server");
							} catch (IOException e1) {
								new ErrorMessage("Could not start, please try with a different port");
							}

						}
					}
					else{
						((Server) serverThread).stop();
						startServer.setText("Start Server");
						try {
							serverSocket.close();
						} catch (IOException e1) {}
					}
				}
			});

		}
	}
}