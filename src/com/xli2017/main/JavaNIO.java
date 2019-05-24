package com.xli2017.main;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Include main GUI and acts like a "client" side
 * @author xli2017
 *
 */
public class JavaNIO extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Logger
	public static Logger logger = MainEntry.logger;
	
	// GUI
	/** Main frame for this program */
	private JPanel mainPanel;
	/** Define main panel width */
	private static final int MAIN_WIDTH = 300; // [pixel]
	/* Define main panel height */
	private static final int MAIN_HEIGHT = 300; // [pixel]
	
	// Functional
	/** Flag for keep checking source pipe */
	private boolean isRunning = false;

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
		this.mainPanel.add(new JLabel("Test"));
		this.add(this.mainPanel);
		
		this.addWindowListener // when close frame
		(
				new java.awt.event.WindowAdapter()
				{
					public void windowClosing(WindowEvent winEvt)
					{
						MainEntry.screenCaptureExecutor.shutdown();
						System.exit(0);
					}
					
				}
		);
	}
	
	private void readPipe(Pipe.SourceChannel sourceChannel) throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(48);
		while(isRunning)
		{
			int bytesRead = sourceChannel.read(buf);
		}
	}
}
