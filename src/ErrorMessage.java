
import java.awt.*;
import javax.swing.*;


import java.awt.event.*;

public class ErrorMessage extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 500;
	private static final int HEIGHT = 100;
	private JButton exitB;
	private JLabel errorMessage;
	
	private ExitButtonHandler ebHandler;
	
	public ErrorMessage(String errorMessage){
		this.errorMessage = new JLabel(errorMessage, SwingConstants.CENTER);
		exitB = new JButton("Okay");
		ebHandler = new ExitButtonHandler();
		exitB.addActionListener(ebHandler);
		Container pane = getContentPane();
		pane.setLayout(new GridLayout(2, 4));
		pane.add(this.errorMessage);
		pane.add(exitB);
		setSize(WIDTH, HEIGHT);
		setVisible(true);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private class ExitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}
	}
	
}