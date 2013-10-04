import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
	public static int height = 550;
	public int width = 1050;
	public static String serverRecievedMessage = "";
	public static String clientRecievedMessage = "";
	// Constructor for GUI
	public VPNGUI() {
		showGUI();
	}

	// Method to display the GUI
	public void showGUI() {

		frame = new JFrame("VPN EECE 412 ASSIGNMENT 3");
		frame.setBackground(Color.white);

		frame.setPreferredSize(new Dimension(width, height));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Panel for viewing the tables
		displayPane = new JPanel();
		displayPane.setBorder(BorderFactory.createLineBorder(Color.black));
		displayPane.setLayout(new BoxLayout(displayPane, BoxLayout.PAGE_AXIS));

		// Panel for displaying types of users
		connectionPane = new JPanel();
		connectionPane.setLayout(new BoxLayout(connectionPane, BoxLayout.Y_AXIS));
		connectionPane.setBorder(BorderFactory.createLineBorder(Color.black));
		Font font = new Font("Arial", Font.BOLD, 16);
		JTextArea connectionTypes = new JTextArea("Connection Type");
		connectionTypes.setFont(font);
		connectionTypes.setEditable(false);
		connectionTypes.setMaximumSize(new Dimension(700, 30));
		connectionPane.add(connectionTypes);

		activitiesPane = new ActivitiesPane();
		activitiesPane.setBorder(BorderFactory.createLineBorder(Color.black));

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

		JButton clientButton = new JButton("Client");
		JButton serverButton = new JButton("Server");

		connectionPane.add(clientButton);
		connectionPane.add(serverButton);

		clientButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activitiesPane.removeAll();
				activitiesPane.updateUI();
				activitiesPane.display("Client");
			}
		});

		serverButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activitiesPane.removeAll();
				activitiesPane.updateUI();
				activitiesPane.display("Server");
			}
		});

	}

	// For displaying the appropriate tables after each transaction
	public static void startVPN(String modeToStartIN) {
		
		JLabel tableTitle = new JLabel();
		//JTable table = null;
		//System.out.println("in here");
		
		if (modeToStartIN == "Client") {
			clientMode();
		}

		if (modeToStartIN == "Server") {
			serverMode();
			
		}

		
	}

	public static void clientMode(){
		JLabel tableTitle = new JLabel("Client");
		JLabel messageSendLabel = new JLabel("Type the message to send: ");
		final JTextField messageToSend = new JTextField();
		messageToSend.setMaximumSize(new Dimension(500,50));
		JButton messageSendButton = new JButton("Send");
		messageSendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client client = new Client(ActivitiesPane.clientSocket);
				client.sendSomeMessages(1, messageToSend.getText().getBytes());
			}
		});
		JLabel messageRecieveLabel = new JLabel("Message from server: ");
		JLabel messageReceivedFromServer = new JLabel(clientRecievedMessage);
		displayPane.removeAll();
		displayPane.updateUI();
		displayPane.add(tableTitle);
		displayPane.add(messageSendLabel);
		displayPane.add(messageToSend);
		displayPane.add(messageSendButton);
		displayPane.add(messageRecieveLabel);
		displayPane.add(messageReceivedFromServer);
	}
	public static void serverMode(){
		JLabel label = new JLabel("Server");
		JLabel serverMessageLabel = new JLabel("Message from client:");
		JLabel tableTitle = new JLabel(serverRecievedMessage);
		JLabel serverSendMessageLabel = new JLabel("Send to client:");
		final JTextField messageToSend = new JTextField();
		messageToSend.setMaximumSize(new Dimension(500,50));
		JButton messageSendButton = new JButton("Send");
		messageSendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Server) ActivitiesPane.serverThread).sendMessage(messageToSend.getText());
			}
		});
		displayPane.removeAll();
		displayPane.updateUI();
		displayPane.add(label);
		displayPane.add(serverMessageLabel);
		displayPane.add(tableTitle);
		displayPane.add(serverSendMessageLabel);
		displayPane.add(messageToSend);
		displayPane.add(messageSendButton);
	}
	
	public static void main(String args[])
    {
      new VPNGUI();
    }
	
}

