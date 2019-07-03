package com.xli2017.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.text.ParseException;
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
//				System.out.println("Received first two bytes: " + imgInByte[0] + " " + imgInByte[1]);
				/* Get current thread name */
				Thread t = Thread.currentThread();
			    String tName = t.getName();
			    /* Get the time stamp of the image sample */
				Date date = DataBox.getDate(DataBox.getHeader(imgInByte));
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
		int bytesRead = sourceChannel.read(this.sourceBuf);
		if(bytesRead > 0) // New data available
		{
//			System.out.println("readPipe method read bytes: " + bytesRead);
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
