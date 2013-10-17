import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.security.SecureRandom;
import javax.swing.*;

public class VPNGUI {
	// The main frame for the GUI
	static JFrame frame;

	// The menubar for the GUI
	private JMenuBar menuBar;

	// All the panels within the frame
	private JPanel connectionPane;
	public static JPanel displayPane;
	private ActivitiesPane activitiesPane;

	// Set dimensions for window
	public static int height = 550;
	public int width = 1050;

	// GUI static variables
	public static JLabel displayEncrypyedClientReceivedMessage;
	public static JLabel displayEncryptedServerReceivedMessage;
	public static JLabel displayClientIV;
	public static JLabel displayServerIV;
	public static JLabel displayClientReceivedMessage;
	public static JLabel displayServerReceivedMessage;
	public static String serverRecievedMessage = "";
	public static String clientRecievedMessage = "";

	// DH mutual authentication variables
	public static BigInteger p = null;
	public static BigInteger g = null;
	public static BigInteger a = null;
	public static BigInteger b = null;
	public static BigInteger serverDHKey = null;
	public static BigInteger clientDHKey = null;
	public static BigInteger DHKey = null;

	// Constructor for GUI
	public VPNGUI() {
		showGUI();
	}

	// Method to display the GUI
	public void showGUI() {

		// Frame settings
		frame = new JFrame("VPN EECE 412 ASSIGNMENT 3");
		frame.setBackground(Color.white);
		frame.setPreferredSize(new Dimension(1000, 500));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMaximumSize(new Dimension(1000, 500));

		// Panel for viewing the messages
		displayPane = new JPanel();
		displayPane.setBorder(BorderFactory.createLineBorder(Color.black));
		displayPane.setLayout(new BoxLayout(displayPane, BoxLayout.PAGE_AXIS));
		displayPane.setMaximumSize(new Dimension(700, 550));

		// Panel for displaying types of users
		connectionPane = new JPanel();
		connectionPane
				.setLayout(new BoxLayout(connectionPane, BoxLayout.Y_AXIS));
		connectionPane.setBorder(BorderFactory.createLineBorder(Color.black));
		Font font = new Font("Arial", Font.BOLD, 16);
		JTextArea connectionTypes = new JTextArea("Connection Type");
		connectionTypes.setFont(font);
		connectionTypes.setEditable(false);
		connectionTypes.setMaximumSize(new Dimension(700, 30));
		connectionPane.add(connectionTypes);

		// Settings pane for user selected
		activitiesPane = new ActivitiesPane();

		// Initialize GUI panels and menu
		initializeMenu();
		initializeUserPane();

		// Adds all the panels to the frame
		frame.getContentPane().add(connectionPane, BorderLayout.WEST);
		frame.getContentPane().add(displayPane, BorderLayout.CENTER);
		frame.getContentPane().add(activitiesPane, BorderLayout.EAST);

		// Shows the frame
		frame.pack();
		frame.setVisible(true);
	}

	// Method for initializing the menu
	private void initializeMenu() {

		JMenu VPN;
		JMenuItem quit;

		VPN = new JMenu("VPN");

		// Exits the application
		quit = new JMenuItem("Quit");
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
			}
		});

		VPN.add(quit);
		menuBar = new JMenuBar();
		menuBar.add(VPN);
		frame.setJMenuBar(menuBar);
	}

	// Method for initializing the activities for each user
	private void initializeUserPane() {

		// Server and client button
		JButton clientButton = new JButton("Client");
		JButton serverButton = new JButton("Server");

		connectionPane.add(clientButton);
		connectionPane.add(serverButton);

		// Show client settings
		clientButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activitiesPane.removeAll();
				activitiesPane.updateUI();
				activitiesPane.display("Client");
			}
		});

		// show server settings
		serverButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activitiesPane.removeAll();
				activitiesPane.updateUI();
				activitiesPane.display("Server");
			}
		});

	}

	// Start Server
	public static void startVPN(String modeToStartIN) {

		// Generate p,g,b and calculate g^b mod p
		if (modeToStartIN == "Server") {
			if (g == null) {
				SecureRandom rnd = new SecureRandom();
				g = BigInteger.probablePrime(32, rnd);
				p = BigInteger.probablePrime(64, rnd);
				b = BigInteger.probablePrime(16, rnd);
				serverDHKey = g.pow(b.intValue()).mod(p);
			}
			// update UI to show server is redy for listening
			waitingForConnection();
		}

	}

	// Display the client mode
	public static void clientMode() {
		frame.setTitle("CLIENT - VPN EECE 412 ASSIGNMENT 3");
		Font font = new Font("Arial", Font.BOLD, 16);
		JLabel tableTitle = new JLabel("Client");
		tableTitle.setFont(font);
		JLabel emptySpace = new JLabel("                  ");
		JLabel nonceSentToServer = new JLabel("Nonce Sent To Server: "
				+ Client.nonce);
		JLabel nonceReceivevedFromServer = new JLabel(
				"Nonce Received From Server: " + Client.serverNonce);
		JLabel encryptedMessageReceivedFromServer = new JLabel(
				"Encrypted DH key exchange: " + Client.encryptedDHKeyExchange);
		JLabel decryptedDHKey = new JLabel("Decrypted DH Key Exchange: "
				+ Client.DHKeyExchange);
		JLabel DHg = new JLabel("g: " + g.toString());
		JLabel DHp = new JLabel("p: " + p.toString());
		JLabel DHa = new JLabel("a: " + a.toString());
		JLabel DHserver = new JLabel("g^b mod p: " + serverDHKey.toString());
		JLabel DHclient = new JLabel("g^a mod p: " + clientDHKey.toString());
		JLabel DHKeyLabel = new JLabel("DHKey (Session Key): "
				+ DHKey.toString());
		JLabel messageSendLabel = new JLabel("Type the message to send: ");
		final JLabel encryptedMessageBeingSent = new JLabel();
		final JLabel IVBeingSent = new JLabel();
		final JTextField messageToSend = new JTextField();
		messageToSend.setMaximumSize(new Dimension(500, 50));
		JButton messageSendButton = new JButton("Send");

		// Send message to server when send button clicked
		messageSendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byte[] encryptedMessage = AES.encrypt(messageToSend.getText(),
						DHKey.toString());
				encryptedMessageBeingSent.setText("Encrypted Message '"
						+ messageToSend.getText() + "': "
						+ encryptedMessage.toString());
				IVBeingSent.setText("IV for encrypted message: "
						+ AES.getIV().toString());
				((Client) ActivitiesPane.clientThread)
						.sendSomeMessages(encryptedMessage);
			}
		});

		JLabel messageRecieveLabel = new JLabel("Message from server: ");
		Font font2 = new Font("Arial", Font.BOLD, 14);
		displayEncrypyedClientReceivedMessage = new JLabel();
		displayClientIV = new JLabel();
		displayClientReceivedMessage = new JLabel();
		displayClientReceivedMessage.setFont(font2);

		displayPane.removeAll();
		displayPane.updateUI();
		displayPane.add(tableTitle);
		displayPane.add(nonceSentToServer);
		displayPane.add(encryptedMessageReceivedFromServer);
		displayPane.add(decryptedDHKey);
		displayPane.add(nonceReceivevedFromServer);
		displayPane.add(DHg);
		displayPane.add(DHp);
		displayPane.add(DHa);
		displayPane.add(DHclient);
		displayPane.add(DHserver);
		displayPane.add(DHKeyLabel);
		displayPane.add(emptySpace);
		displayPane.add(messageSendLabel);
		displayPane.add(messageToSend);
		displayPane.add(messageSendButton);
		displayPane.add(encryptedMessageBeingSent);
		displayPane.add(IVBeingSent);
		displayPane.add(messageRecieveLabel);
		displayPane.add(displayEncrypyedClientReceivedMessage);
		displayPane.add(displayClientIV);
		displayPane.add(displayClientReceivedMessage);
	}

	// Display the server view
	public static void serverMode() {
		frame.setTitle("SERVER - VPN EECE 412 ASSIGNMENT 3");
		JLabel label = new JLabel("Server");
		Font font = new Font("Arial", Font.BOLD, 16);
		label.setFont(font);
		JLabel emptySpace = new JLabel("                  ");
		JLabel nonceSentToClient = new JLabel("Nonce Sent To Client: "
				+ Server.nonce);
		JLabel nonceReceivevedFromClient = new JLabel(
				"Nonce Received From Client: " + Server.nonceFromClient);
		JLabel encryptedMessageReceivedFromClient = new JLabel(
				"Encrypted DH key exchange: " + Server.encryptedDHKeyExchange);
		JLabel decryptedDHKey = new JLabel("Decrypted DH Key Exchange: "
				+ Server.DHKeyExchange);
		JLabel DHg = new JLabel("g: " + g.toString());
		JLabel DHp = new JLabel("p: " + p.toString());
		JLabel DHb = new JLabel("b: " + b.toString());
		JLabel DHserver = new JLabel("g^b mod p: " + serverDHKey.toString());
		JLabel DHclient = new JLabel("g^a mod p: " + clientDHKey.toString());
		JLabel DHKeyLabel = new JLabel("DHKey (Session Key): "
				+ DHKey.toString());
		JLabel serverMessageLabel = new JLabel("Message from client:");
		JLabel serverSendMessageLabel = new JLabel("Send to client:");
		final JLabel encryptedMessageBeingSent = new JLabel();
		final JLabel IVBeingSent = new JLabel();
		final JTextField messageToSend = new JTextField();
		messageToSend.setMaximumSize(new Dimension(500, 50));
		JButton messageSendButton = new JButton("Send");

		// Send message to client
		messageSendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byte[] encryptedMessage = AES.encrypt(messageToSend.getText(),
						DHKey.toString());
				System.out.println("entext:" + encryptedMessage);
				encryptedMessageBeingSent.setText("Encrypted Message '"
						+ messageToSend.getText() + "': "
						+ encryptedMessage.toString());
				IVBeingSent.setText("IV for encrypted message: "
						+ AES.getIV().toString());
				((Server) ActivitiesPane.serverThread)
						.sendMessage(encryptedMessage);
			}
		});

		Font font2 = new Font("Arial", Font.BOLD, 14);

		displayEncryptedServerReceivedMessage = new JLabel();
		displayServerIV = new JLabel();
		displayServerReceivedMessage = new JLabel();
		displayServerReceivedMessage.setFont(font2);

		displayPane.removeAll();
		displayPane.updateUI();
		displayPane.add(label);
		displayPane.add(nonceSentToClient);
		displayPane.add(encryptedMessageReceivedFromClient);
		displayPane.add(decryptedDHKey);
		displayPane.add(nonceReceivevedFromClient);
		displayPane.add(DHg);
		displayPane.add(DHp);
		displayPane.add(DHb);
		displayPane.add(DHserver);
		displayPane.add(DHclient);
		displayPane.add(DHKeyLabel);
		displayPane.add(emptySpace);
		displayPane.add(serverSendMessageLabel);
		displayPane.add(messageToSend);
		displayPane.add(messageSendButton);
		displayPane.add(encryptedMessageBeingSent);
		displayPane.add(IVBeingSent);
		displayPane.add(serverMessageLabel);
		displayPane.add(displayEncryptedServerReceivedMessage);
		displayPane.add(displayServerIV);
		displayPane.add(displayServerReceivedMessage);
	}

	// Display text "waiting for connection"
	public static void waitingForConnection() {
		displayPane.removeAll();
		displayPane.updateUI();
		displayPane.add(new JLabel("Waiting for connection..."));

	}

	// Main method
	public static void main(String args[]) {
		new VPNGUI();
	}

}
