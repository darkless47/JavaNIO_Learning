package com.xli2017.main;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;

import javax.imageio.ImageIO;

/** 
 * To get the capture of screen
 * @author xli2017
 *
 */
public class ScreenCapture implements Runnable
{
	/** The width of desired screen capture area */
	private int scopeWidth = 200; // [pixels]
	/** The height of desired screen capture area */
	private int scopeHeight = 200; // [pixels]
	/** Current screen size */
	private Dimension dimension;
	/** System Direct allocated buffer for Java NIO */
	private ByteBuffer buf;
	
	/**
	 * Constructor
	 */
	public ScreenCapture()
	{
		/* Toolkit to get current screen size */
		Toolkit tk = Toolkit.getDefaultToolkit();
		dimension = tk.getScreenSize();
		MainEntry.logger.log(Level.FINER, "Screen size is: " + dimension.width + " " + dimension.height);
		
		/* System Direct allocated buffer for Java NIO */
		this.buf = ByteBuffer.allocateDirect(MainEntry.BUFFER_SIZE);
	}
	
	/**
	 * For Runnable
	 */
	@Override
	public void run()
	{
		BufferedImage img = captureScreen(scopeAroundCursor());
		byte[] imgInByte = null;
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "jpg", baos);
			baos.flush();
			imgInByte = baos.toByteArray();
			/* Add date header here */
			Date date = new Date();
//			MainEntry.logger.log(Level.FINE, "Sent " + Integer.toString(imgInByte.length));
			baos.close();
			imgInByte = DataBox.addHeader(imgInByte, date);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if(imgInByte != null)
		{
//			System.out.println("Sent first two bytes: " + imgInByte[0] + " " + imgInByte[1]);
			/* Use the buffer to store the data that going to be sent */
			this.buf.clear();
			this.buf.put(imgInByte);
			/* Flip so that the buffer index can get ready for sending */
			this.buf.flip();
			
			while(this.buf.hasRemaining())
			{
			    try
			    {
					MainEntry.sinkChannel_0.write(this.buf);
				}
			    catch (IOException e)
			    {
					e.printStackTrace();
				}
			}
			this.buf.compact();
		}
		else
		{
			MainEntry.logger.log(Level.SEVERE, "Error in converting buffered image!");
		}
	}
	
	/**
	 * Obtain the dimension of scope
	 * @return Dimension(width, height)
	 */
	public Dimension getScopeDimension()
	{
		return new Dimension(this.scopeWidth, this.scopeHeight);
	}
	
	/**
	 * Capture an area from current screen
	 * @return the screenshot image
	 */
	public static BufferedImage captureScreen(Rectangle rec)
	{ 
        try
        {
        	/* Screen capture robot */
            Robot rb = new Robot();  
            /* Read screen */
            BufferedImage bi = rb.createScreenCapture(rec);  
            return bi;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
	
	private Rectangle scopeAroundCursor()
	{
		/* Get position of mouse cursor */
		Point cursorPoint = java.awt.MouseInfo.getPointerInfo().getLocation();
		MainEntry.logger.log(Level.FINEST, "Cursor is at " + cursorPoint.x + " " + cursorPoint.y);
		/* Calculate 4 corners of the area of capturing */
		int[] points = {cursorPoint.x - scopeWidth/2, cursorPoint.y - scopeHeight/2,
				cursorPoint.x + scopeWidth/2, cursorPoint.y - scopeHeight/2,
				cursorPoint.x - scopeWidth/2, cursorPoint.y + scopeHeight/2,
				cursorPoint.x + scopeWidth/2, cursorPoint.y + scopeHeight/2};
		/* Boundary check */
		for(int i = 0; i < 8; i++)
		{
			if(points[i] < 0)
			{
				points[i] = 0;
			}
			if(i%2 == 0) // for x value
			{
				if(points[i] > this.dimension.width)
				{
					points[i] = this.dimension.width;
				}
			}
			else // for y value
			{
				if(points[i] > this.dimension.height)
				{
					points[i] = this.dimension.height;
				}
			}
		}
		int[] upperLeft = {points[0], points[1]};
//		System.out.println(upperLeft[0] + " " + upperLeft[1]);
//		int[] upperRight = {points[2], points[3]};
//		int[] lowerLeft = {points[4], points[5]};
//		int[] lowerRight = {points[6], points[7]};
		
		/* Build the rectangle */
        Rectangle rec = new Rectangle(upperLeft[0], upperLeft[1], this.scopeWidth, this.scopeHeight);
        return rec;
	}
	
}
