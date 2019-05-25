package com.xli2017.main;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Include main GUI and acts like a "client" side
 * @author xli2017
 *
 */
public class JavaNIO extends JFrame implements Runnable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Logger
	public static Logger logger = MainEntry.logger;
	
	// GUI
	/** Define main panel width */
	private static final int MAIN_WIDTH = 300; // [pixel]
	/* Define main panel height */
	private static final int MAIN_HEIGHT = 300; // [pixel]
	/** Main frame for this program */
	private JPanel mainPanel;
	/** Start button */
	private JButton button_Start;
	
	// Functional
	/** Flag for keep checking source pipe */
	private boolean isRunning = false;
	
	private ByteBuffer buf;

	/**
	 * Constructor
	 */
	public JavaNIO()
	{	
		// GUI
		this.setTitle("JavaNIO Learning");
		this.setLayout(new GridLayout(1, 1));
		this.setResizable(true);
		Dimension screenSize =Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(screenSize.width/2-(JavaNIO.MAIN_WIDTH + 10)/2,screenSize.height/2-(JavaNIO.MAIN_HEIGHT + 10)/2);
		this.setSize(JavaNIO.MAIN_WIDTH + 10, JavaNIO.MAIN_HEIGHT + 10);
		this.setVisible(true);
		
		this.mainPanel = new JPanel(new FlowLayout());
		this.mainPanel.setBorder(new TitledBorder("Main Panel"));
		this.mainPanel.setPreferredSize(new Dimension(JavaNIO.MAIN_WIDTH, JavaNIO.MAIN_HEIGHT));
		this.button_Start = new JButton("Start");
		this.button_Start.addActionListener(new StartButtonListener());
		this.mainPanel.add(this.button_Start);
		this.add(this.mainPanel);
		
		this.addWindowListener // when close frame
		(
				new java.awt.event.WindowAdapter()
				{
					public void windowClosing(WindowEvent winEvt)
					{
						System.exit(0);
					}
					
				}
		);
		this.revalidate();
		
		// Allocate the buffer
		this.buf = ByteBuffer.allocateDirect(100000);
	}
	
	/**
	 * For runnable
	 */
	@Override
	public void run()
	{
		byte[] imgInByte = null;
		while(this.isRunning)
		{
			try
			{
				imgInByte = readPipe(MainEntry.sourceChannel_0);
				if (imgInByte != null) // New data comes
				{
					ByteArrayInputStream bais = new ByteArrayInputStream(imgInByte);
					BufferedImage img = ImageIO.read(bais);
					JavaNIO.logger.log(Level.FINE, "Received: " + Integer.toString(imgInByte.length));
					//TODO image process
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Read the source pipe
	 * @param sourceChannel Source channel of a pipe where data comes
	 * @return An byte array that stores the data if there is new data; null for no new data
	 * @throws IOException
	 */
	private byte[] readPipe(Pipe.SourceChannel sourceChannel) throws IOException
	{
		byte[] imgInByte = new byte[100000];
		int bytesRead = sourceChannel.read(this.buf);
		if(bytesRead > 0) // New data available
		{
			this.buf.flip();
			while(this.buf.hasRemaining())
			{
				this.buf.get(imgInByte);
			}
			this.buf.clear();
			return imgInByte;
		}
		else // No new data
		{
			return null;
		}
	}
	
	/**
	 * Start button action listener
	 * @author xli2017
	 *
	 */
	class StartButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (isRunning) // Running status
			{
				// Turn to OFF
				isRunning = false;
				button_Start.setText("Start");
				JavaNIO.logger.log(Level.FINE, "Pipe reading stopped.");
			}
			else // Stop status
			{
				// Turn to ON
				isRunning = true;
				button_Start.setText("Stop");
				run();
				JavaNIO.logger.log(Level.FINE, "Pipe reading starts.");
			}
		}
	}
}
