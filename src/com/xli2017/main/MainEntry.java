package com.xli2017.main;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry of this program
 * Deal with the threads configuration, then launch them
 * 
 * @author xli2017
 *
 */
public class MainEntry
{
	/** The number of threads will be used to run main panel task */
	public static final int NUMBER_THREAD_MAIN_PANEL = 1;
	/** The time step between two updates of main panel */
	public static final int MAIN_PANEL_TIME_STEP = 100; // [milliseconds]
	/** The number of threads will be used to run screen capture task */
	public static final int NUMBER_THREAD_SCREEN_CAP = 1;
	/** The time step between two screen captures */
	public static final int CAPTURE_TIME_STEP = 100; // [milliseconds]
	
	// Logger
	public static Logger logger = Logger.getLogger(MainEntry.class.getName());
	public static ConsoleHandler handler = new ConsoleHandler();
	public static Level loggerLevel = Level.FINE;
	public static Level handlerLevel = Level.FINE;
	
	public static Pipe pipe_0;
	public static Pipe.SinkChannel sinkChannel_0;
	public static Pipe.SourceChannel sourceChannel_0;
	
	/** Thread pool used to execute main panel in a fixed rate */
	private PausableThreadPoolExecutor mainPanelExecutor;
	/** For main panel thread */
	private JavaNIO javaNIO;
	/** Thread pool used to execute screen capture in a fixed rate */
	private PausableThreadPoolExecutor screenCaptureExecutor;
	/** For Screen capture thread */
	private ScreenCapture screenCapture;
	/** String builder for the message */
	private StringBuilder str;
	/** Flag for thread executor status */
	private boolean isExecutorStarted = false;
	
	/**
	 * Constructor
	 */
	public MainEntry()
	{
		
	}
	
	/**
	 * Set logger, configure pipe, launch threads
	 */
	public void run()
	{
		// Set the logger
		MainEntry.logger.addHandler(MainEntry.handler);
		MainEntry.logger.setLevel(MainEntry.loggerLevel);
		MainEntry.handler.setLevel(MainEntry.handlerLevel);
		
		// Initial the pipe
		try
		{
			// Open the pipe
			pipe_0 = Pipe.open();
			// Set the sink and source pipe to blocking mode
			pipe_0.sink().configureBlocking(false);
			pipe_0.source().configureBlocking(false);
			sinkChannel_0 = pipe_0.sink();
			sourceChannel_0 = pipe_0.source();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Main thread
		this.javaNIO = new JavaNIO(this);
		this.mainPanelExecutor = new PausableThreadPoolExecutor(NUMBER_THREAD_MAIN_PANEL);
		
		
		
		
		// Screen capture thread
		this.screenCapture = new ScreenCapture();
		this.screenCaptureExecutor = new PausableThreadPoolExecutor(NUMBER_THREAD_SCREEN_CAP);
		
	}
	
	public void startRunning()
	{
		if (this.isExecutorStarted) // On paused
		{
			this.mainPanelExecutor.resume();
			this.screenCaptureExecutor.resume();
		}
		else // First time run
		{
			this.mainPanelExecutor.scheduleAtFixedRate(this.javaNIO, 0, MAIN_PANEL_TIME_STEP, TimeUnit.MILLISECONDS);
			this.screenCaptureExecutor.scheduleAtFixedRate(this.screenCapture, 0, CAPTURE_TIME_STEP, TimeUnit.MILLISECONDS);
			this.isExecutorStarted = true;
		}
		this.str = new StringBuilder();
		this.str.append("\n\tJavaNIO thread is running.\n\tScreen capture thread is running.");
		MainEntry.logger.log(Level.FINE, str.toString());
	}
	
	public void stopRunning()
	{
		this.mainPanelExecutor.pause();
		this.screenCaptureExecutor.pause();
//		try
//		{
//			
//			this.mainPanelExecutor.shutdownNow();
//			this.screenCaptureExecutor.shutdownNow();
//			this.mainPanelExecutor.awaitTermination(300, TimeUnit.MILLISECONDS);
//			this.screenCaptureExecutor.awaitTermination(300, TimeUnit.MILLISECONDS);
//			
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
		this.str = new StringBuilder();
		this.str.append("\n\tJavaNIO thread stopped.\n\tScreen capture thread stopped.");
		MainEntry.logger.log(Level.FINE, str.toString());
	}

}
