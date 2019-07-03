package com.xli2017.main;

import java.io.IOException;
import java.nio.channels.Pipe;
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
	/** The size of the byte buffer to store data for pipe */
	public static final int BUFFER_SIZE = 300000; // 300,000 is for 1920x1080, it should enough
	/** The number of threads will be used to run main panel task */
	public static final int NUMBER_THREAD_MAIN_PANEL = 1;
	/** The time step between two updates of main panel */
	public static final int MAIN_PANEL_TIME_STEP = 100; // [milliseconds]
	/** The number of threads will be used to run screen capture task */
	public static final int NUMBER_THREAD_SCREEN_CAP = 1;
	/** The time step between two screen captures */
	public static final int CAPTURE_TIME_STEP = 100; // [milliseconds]
	/** The number of threads will be used to run image processor */
	public static final int NUMBER_THREAD_IMAGE_PROC = 2;
	
	/* Logger */
	public static Logger logger = Logger.getLogger(MainEntry.class.getName());
	public static ConsoleHandler handler = new ConsoleHandler();
	public static Level loggerLevel = Level.FINE;
	public static Level handlerLevel = Level.FINE;
	
	/* Date */
	/** Create a thread-safe synchronized date instance */
	public static DateSyncUtil dateUtil = new DateSyncUtil();
	
	/** Pipe 0 for transmitting data between image capture thread and image process thread */
	public static Pipe pipe_0;
	public static Pipe.SinkChannel sinkChannel_0;
	public static Pipe.SourceChannel sourceChannel_0;
	
	/** Pipe 1 for transmitting data between image process thread and GUI thread */
	public static Pipe pipe_1;
	public static Pipe.SinkChannel sinkChannel_1;
	public static Pipe.SourceChannel sourceChannel_1;
	
	/** Thread pool used to execute main panel in a fixed rate */
	private PausableThreadPoolExecutor mainPanelExecutor;
	/** For main panel thread */
	private JavaNIO javaNIO;
	/** Thread pool used to execute screen capture in a fixed rate */
	private PausableThreadPoolExecutor screenCaptureExecutor;
	/** For Screen capture thread */
	public static ScreenCapture screenCapture;
	/** Thread pool used to execute image processor */
	private PausableThreadPoolExecutor[] imageProcessExecutor;
	/** For image process thread */
	private ImageProcessor[] imageProcessor;
	
	
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
		/* Set the logger */
		MainEntry.logger.addHandler(MainEntry.handler);
		MainEntry.logger.setLevel(MainEntry.loggerLevel);
		MainEntry.handler.setLevel(MainEntry.handlerLevel);
		
		/* Initial the pipe */
		try
		{
			/* Open the pipe 0 */
			pipe_0 = Pipe.open();
			/* Set the sink and source pipe to blocking mode */
			pipe_0.sink().configureBlocking(false);
			pipe_0.source().configureBlocking(false);
			sinkChannel_0 = pipe_0.sink();
			sourceChannel_0 = pipe_0.source();
			
			/* Open the pipe 1 */
			pipe_1 = Pipe.open();
			/* Set the sink and source pipe to blocking mode */
			pipe_1.sink().configureBlocking(false);
			pipe_1.source().configureBlocking(false);
			sinkChannel_1 = pipe_1.sink();
			sourceChannel_1 = pipe_1.source();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		/* Main thread */
		this.javaNIO = new JavaNIO(this);
		this.mainPanelExecutor = new PausableThreadPoolExecutor(NUMBER_THREAD_MAIN_PANEL);
		/* Screen capture thread */
		MainEntry.screenCapture = new ScreenCapture();
		this.screenCaptureExecutor = new PausableThreadPoolExecutor(NUMBER_THREAD_SCREEN_CAP);
		/* Image process thread */
		this.imageProcessor = new ImageProcessor[NUMBER_THREAD_IMAGE_PROC];
		this.imageProcessExecutor = new PausableThreadPoolExecutor[NUMBER_THREAD_IMAGE_PROC];
		for(int i = 0; i < NUMBER_THREAD_IMAGE_PROC; i++)
		{
			this.imageProcessor[i] = new ImageProcessor(MainEntry.sourceChannel_0);
			this.imageProcessExecutor[i] = new PausableThreadPoolExecutor(1);
		}
	}
	
	/**
	 * The method to start or resume the executor(s)
	 * @see PausableThreadPoolExecutor
	 */
	public void startRunning()
	{
		/* Check if executor(s) just been paused */
		if (this.isExecutorStarted) // On paused
		{
			this.mainPanelExecutor.resume();
			this.screenCaptureExecutor.resume();
			for(int i = 0; i < NUMBER_THREAD_IMAGE_PROC; i++)
			{
				this.imageProcessExecutor[i].resume();
			}
			
		}
		else // First time run
		{
			/* Start executor(s) */
			this.mainPanelExecutor.scheduleAtFixedRate(this.javaNIO, 0, MAIN_PANEL_TIME_STEP, TimeUnit.MILLISECONDS);
			this.screenCaptureExecutor.scheduleAtFixedRate(MainEntry.screenCapture, 0, CAPTURE_TIME_STEP, TimeUnit.MILLISECONDS);
			for(int i = 0; i < NUMBER_THREAD_IMAGE_PROC; i++)
			{
				this.imageProcessExecutor[i].execute(this.imageProcessor[i]);
			}
			
			this.isExecutorStarted = true;
		}
		this.str = new StringBuilder();
		this.str.append("\n\tJavaNIO thread is running.\n\tScreen capture thread is running.");
		MainEntry.logger.log(Level.FINE, str.toString());
	}
	
	/**
	 * The method to pause the executor(s)
	 * @see PausableThreadPoolExecutor
	 */
	public void stopRunning()
	{
		this.mainPanelExecutor.pause();
		this.screenCaptureExecutor.pause();
		for(int i = 0; i < NUMBER_THREAD_IMAGE_PROC; i++)
		{
			this.imageProcessExecutor[i].pause();
		}
		this.str = new StringBuilder();
		this.str.append("\n\tJavaNIO thread stopped.\n\tScreen capture thread stopped.");
		MainEntry.logger.log(Level.FINE, str.toString());
	}

}
