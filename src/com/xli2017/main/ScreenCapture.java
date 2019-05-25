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
		// Toolkit to get current screen size
		Toolkit tk = Toolkit.getDefaultToolkit();
		dimension = tk.getScreenSize();
		
		// System Direct allocated buffer for Java NIO
		this.buf = ByteBuffer.allocateDirect(100000);
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
			MainEntry.logger.log(Level.FINE, "Sent " + Integer.toString(imgInByte.length));
			baos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if(imgInByte != null)
		{
			
			this.buf.clear();
			this.buf.put(imgInByte);
			this.buf.flip();
			while(buf.hasRemaining())
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
		}
		else
		{
			MainEntry.logger.log(Level.SEVERE, "Error in converting buffered image!");
		}
	}
	
	/**
	 * Capture an area from current screen
	 * @return the screenshot image
	 */
	public static BufferedImage captureScreen(Rectangle rec)
	{ 
        try
        {
        	// Screen capture robot
            Robot rb = new Robot();  
            // Read screen
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
		// Get position of mouse cursor
		Point cursorPoint = java.awt.MouseInfo.getPointerInfo().getLocation();
		// Calculate 4 corners of the area of capturing
		int[] points = {cursorPoint.x - scopeWidth/2, cursorPoint.y - scopeHeight/2,
				cursorPoint.x + scopeWidth/2, cursorPoint.y - scopeHeight/2,
				cursorPoint.x - scopeWidth/2, cursorPoint.y + scopeHeight/2,
				cursorPoint.x + scopeWidth/2, cursorPoint.y + scopeHeight/2};
		// Boundary check
		for(int i = 0; i < 8; i++)
		{
			if(points[i] < 0)
			{
				points[i] = 0;
			}
			if(i%2 == 0) // for x value
			{
				if(points[i] > dimension.width)
				{
					points[i] = dimension.width;
				}
			}
			else // for y value
			{
				if(points[i] > dimension.height)
				{
					points[i] = dimension.height;
				}
			}
		}
		int[] leftTop = {points[0], points[1]};
//		int[] rightTop = {points[2], points[3]};
//		int[] leftBottom = {points[4], points[5]};
		int[] rightBottom = {points[6], points[7]};
		
		// Build the rectangle
        Rectangle rec = new Rectangle(leftTop[0], leftTop[1], rightBottom[0], rightBottom[1]);
        return rec;
	}
}
