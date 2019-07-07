package com.xli2017.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

/**
 * Class to process the image sample
 * Can have multiple instances
 * @author xli2017
 *
 */
public class ImageProcessor implements Runnable
{
	/** Source of image data */
	private Pipe.SourceChannel sourceChannel;
	/** The buffer used to save data comes from source channel */
	private ByteBuffer sourceBuf;
	/** The buffer used to send data to sink channel */
	private ByteBuffer sinkBuf;
	
	/**
	 * Constructor
	 */
	public ImageProcessor(Pipe.SourceChannel sourceChannelInput)
	{
		this.sourceChannel = sourceChannelInput;
		/* System Direct allocated buffer for Java NIO */
		this.sourceBuf = ByteBuffer.allocateDirect(MainEntry.BUFFER_SIZE);
		this.sinkBuf = ByteBuffer.allocateDirect(MainEntry.BUFFER_SIZE);
	}
	
	/**
	 * For Runnable
	 */
	@Override
	public void run()
	{
		byte[] imgInByte = null;
		/* Get current thread name */
		Thread t = Thread.currentThread();
	    String tName = t.getName();
		
		/* Constant loop */
		while(true)
		{
			try
			{
				/* Try to read data from pipe */
				imgInByte = this.readPipe(this.sourceChannel); // This is a synchronized method
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if (imgInByte != null) // New data comes
			{
				System.out.println("Received first two bytes: " + imgInByte[0] + " " + imgInByte[1]);
			    /* Get the time stamp of the image sample */
			    ArrayList<byte[]> headerArrayList = DataBox.getHeader(imgInByte);
			    if (headerArrayList != null)
			    {
			    	Date date = DataBox.getDate(headerArrayList);
					String dateString = null;
					try
					{
						/* Format the date to get desired format "yyyy-MM-dd HH:mm:ss.SSS" */
						dateString = DateSyncUtil.formatDate(date);
					}
					catch (ParseException e1)
					{
						e1.printStackTrace();
					}
					MainEntry.logger.log(Level.FINE, tName + " received: " + imgInByte.length + ". Timestamp is " + dateString);
					
					/* Get image data */
					imgInByte = DataBox.getData(imgInByte);
					
					/* Send data to GUI */
					this.sinkBuf.clear();
					this.sinkBuf.put(imgInByte);
					this.sinkBuf.flip();
					while(this.sinkBuf.hasRemaining())
					{
					    try
					    {
							MainEntry.sinkChannel_1.write(this.sinkBuf);
						}
					    catch (IOException e)
					    {
							e.printStackTrace();
						}
					}
			    }
			    else
			    {
			    	MainEntry.logger.log(Level.WARNING, "From " + tName + ", Bad data have been abandoned!");
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
		/* How many bytes has been read into the buffer */
		int bytesRead = sourceChannel.read(this.sourceBuf);
		/* How many times did this method try to read from channel */
		int attemptTimes = 1;
		
		if (bytesRead > 0) // New data available
		{
			/* Read last byte of the data */
			byte lastByte = this.sourceBuf.get(bytesRead - 1);
			
			while ((lastByte != DataBox.SEPARATOR_PACKAGE) && (attemptTimes < 5)) // Did not received an entire data within 5 times attempts
			{
				/* Read again */
				bytesRead = bytesRead + sourceChannel.read(this.sourceBuf);
				/* Check the last byte again */
				lastByte = this.sourceBuf.get(bytesRead - 1);
				/* Attempt time increase */
				attemptTimes++;
			}
			/* Can not get an entire data with a valid separator after ran out of the max number of attempts */
			if (lastByte != DataBox.SEPARATOR_PACKAGE)
			{
				MainEntry.logger.log(Level.WARNING, "The readPipe method did not receive an entire data from the sourceChannel");
			}
			
			/* Get current thread name */
			Thread t = Thread.currentThread();
		    String tName = t.getName();
			System.out.println(tName + " readPipe method read bytes: " + bytesRead);
			byte[] imgInByte = new byte[bytesRead];
			int index = 0; // The index of imgInByte
			this.sourceBuf.flip();
			while(this.sourceBuf.hasRemaining())
			{
				imgInByte[index] = this.sourceBuf.get();
				index++;
			}
			this.sourceBuf.clear();
			return imgInByte;
		}
		else // No new data
		{
			return null;
		}
	}
	
}
