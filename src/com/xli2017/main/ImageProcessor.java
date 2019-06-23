package com.xli2017.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.logging.Level;

/**
 * Class to process the image sample
 * Can have multiple instances
 * @author xli2017
 *
 */
public class ImageProcessor implements Runnable
{
	/** Denote if this instance is working */
	private boolean isWorking;
	/** Denote if image data is available from source channel */
	private boolean isImageDataReady;
	/** Source of image data */
	private Pipe.SourceChannel sourceChannel;
	/** The buffer used to save data comes from source channel */
	private ByteBuffer buf;
	
	/**
	 * Constructor
	 */
	public ImageProcessor(Pipe.SourceChannel sourceChannelInput)
	{
		this.isWorking = false;
		this.isImageDataReady = false;
		this.sourceChannel = sourceChannelInput;
		// System Direct allocated buffer for Java NIO
		this.buf = ByteBuffer.allocateDirect(MainEntry.BUFFER_SIZE);
	}
	
	/**
	 * For Runnable
	 */
	@Override
	public void run()
	{
		// Check if this instance is working
		if(this.isWorking) // Working
		{
			
		}
		else // Not working
		{
			byte[] imgInByte = null;
			while(!this.isImageDataReady) // When image data doesn't show at the source pipe
			{
				try
				{
					// This is a synchronized method
					imgInByte = this.readPipe(this.sourceChannel);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
				if (imgInByte != null) // New data comes
				{
					// Set flags to true
					this.isImageDataReady = true;
					this.isWorking = true;
					// Get current thread name
					Thread t = Thread.currentThread();
				    String tName = t.getName();
					MainEntry.logger.log(Level.FINE, tName + " received: " + imgInByte.length);
					this.isWorking = false;
					this.isImageDataReady = false;
				}
			}
		}
		
	}
	
	/**
	 * Read the source pipe
	 * @param sourceChannel Source channel of a pipe where data comes
	 * @return An byte array that stores the data if there is new data; null for no new data
	 * @throws IOException
	 */
	private synchronized byte[] readPipe(Pipe.SourceChannel sourceChannel) throws IOException
	{
		int bytesRead = sourceChannel.read(this.buf);
		if(bytesRead > 0) // New data available
		{
			byte[] imgInByte = new byte[bytesRead];
			int index = 0; // The index of imgInByte
			this.buf.flip();
			while(this.buf.hasRemaining())
			{
				imgInByte[index] = this.buf.get();
				index++;
			}
			this.buf.clear();
			return imgInByte;
		}
		else // No new data
		{
			return null;
		}
	}
	
}
