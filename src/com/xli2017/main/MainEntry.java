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
	/** The number of threads will be used to run screen capture task */
	public static final int NUMBER_THREAD_SCREEN_CAP = 1;
	/** The time step between two screen captures */
	public static final int CAPTURE_TIME_STEP = 100; // [milliseconds]
	
	// Logger
	public static Logger logger = Logger.getLogger(MainEntry.class.getName());
	public static ConsoleHandler handler = new ConsoleHandler();
	public static Level loggerLevel = Level.FINE;
	public static Level handlerLevel = Level.FINE;
	
	public static JavaNIO javaNIO;
	
	public static Thread thread_0;
	
	public static Pipe pipe_0;
	public static Pipe.SinkChannel sinkChannel_0;
	public static Pipe.SourceChannel sourceChannel_0;
	
	/** Thread pool used to execute screen capture in a fixed rate */
	private ScheduledThreadPoolExecutor screenCaptureExecutor;
	/** For Screen capture thread */
	private ScreenCapture screenCapture;
	
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
		MainEntry.thread_0 = new Thread(new JavaNIO());
		MainEntry.thread_0.start();
		StringBuilder str = new StringBuilder();
		str.append("JavaNIO thread is running.");
		MainEntry.logger.log(Level.FINE, str.toString());
		
		
		// Screen capture thread
		this.screenCapture = new ScreenCapture();
		this.screenCaptureExecutor = new ScheduledThreadPoolExecutor(NUMBER_THREAD_SCREEN_CAP);
		this.screenCaptureExecutor.scheduleAtFixedRate(this.screenCapture, 0, CAPTURE_TIME_STEP, TimeUnit.MILLISECONDS);
		str = new StringBuilder();
		str.append("Screen capture thread is running.");
		MainEntry.logger.log(Level.FINE, str.toString());
	}

}
