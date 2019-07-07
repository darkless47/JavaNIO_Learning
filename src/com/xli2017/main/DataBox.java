package com.xli2017.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

/**
 * Includes several methods that help to build byte array with header added from original data
 * Header may includes:
 * 		length of header
 * 		time stamp
 * 		comes from which thread
 * 		processing time
 * 
 * @author xli2017
 *
 */
public abstract class DataBox
{
	/** Package separator character */
	public static final byte SEPARATOR_PACKAGE = 28; // Dec 28 = (file separator)
	/** Section separator character */
	public static final byte SEPARATOR_SECTION = 30; // Dec 30 = (record separator)
	/** For header byte array list, to locate the section for length */
	public static final int SECTION_NUMBER_LENGTH = 0;
	/** For header byte array list, to locate the section for time and date */
	public static final int SECTION_NUMBER_DATE = 1;
	
	/**
	 * Add header to a byte array of data and also add the end sign (Package separator character) to the data
	 * @param original Original data byte array
	 * @param date Time stamp
	 * @return Concatenated byte array
	 */
	public static byte[] addHeader(byte[] original, Date date)
	{
		/* Header length byte */
		byte[] headerLengthByte = null;
		
		/* Get time byte */
    	String dateStr = null;
		try
		{
			/* Format date before send 
			 * Use this synchronized method to get date
			 * */
			dateStr = DateSyncUtil.formatDate(date);
		}
		catch (ParseException e1)
		{
			e1.printStackTrace();
		}
		/* Get the byte array so that it can be added to another byte array that contains image data */ 
		byte dateByte[] = dateStr.getBytes();
		
		/*
		 * Following calculation:
		 * 2 - Two digits for header length, 0 ~ 99
		 * 1 - Separator
		 * 
		 * Header length (2) + Section separator (1) + dateByte.length + Section separator (1)
		 */
		int headerLength = 2 + 1 + dateByte.length + 1;
		if(headerLength > 99) // Have a too large header length which is impossible for current version program
		{
			MainEntry.logger.log(Level.SEVERE, "The value of header length is too large.");
			return null;
		}
		else
		{
			/* Get the header length in an byte array format */
			headerLengthByte = Integer.toString(headerLength).getBytes();
//			System.out.println(headerLengthByte[0] + " " + headerLengthByte[1]);
		}
		
		/* Output stream for concatenating */
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			outputStream.write(headerLengthByte); // Header length
			outputStream.write(DataBox.SEPARATOR_SECTION); // Separator
			outputStream.write(dateByte); // Write time first
			outputStream.write(DataBox.SEPARATOR_SECTION); // Separator
			outputStream.write(original); // Data
			outputStream.write(DataBox.SEPARATOR_PACKAGE); // Separator
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		/* Byte array going to be returned */
		byte[] result = outputStream.toByteArray();
		try
		{
			outputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Get original data from a boxed byte array
	 * @param original Boxed byte array with header
	 * @return Original data
	 */
	public static byte[] getData(byte[] bytesWithHeader)
	{
		/* bytes going to be returned */
		byte[] result = null;
		/*Get header length */
		int headerLength = getHeaderLength(bytesWithHeader);
		/* Check if it can not get a valid length value of the header */
		if (headerLength != -1) // -1 means failed to get the length value
		{
			/* Only copy data part of the bytes */
			result = Arrays.copyOfRange(bytesWithHeader, headerLength, bytesWithHeader.length - 1); // -1 because the last byte is always separator if everything goes well
			/* Return the result bytes */
			return result;
		}
		else // Failed to get the length value
		{
			/* Return null */
			return result;
		}
	}
	
	public static ArrayList<byte[]> getHeader(byte[] bytesWithHeader)
	{
		/*Get header length */
		int headerLength = getHeaderLength(bytesWithHeader);
		/* Check if it can not get a valid length value of the header */
		if (headerLength != -1) // -1 means failed to get the length value
		{
			/* Only copy header part of the bytes */
			byte[] header = Arrays.copyOfRange(bytesWithHeader, 0, headerLength);
//			System.out.println(header.length);
//			System.out.println(header[header.length - 1]);
			
			ArrayList<byte[]> headerByteArrayList = new ArrayList<byte[]>();
			
			/* Indicate which section the index is in
			 * section 0 = header length bytes
			 * section 1 = time bytes
			 * 
			 * */
			int section = 0;
			/* Indicate the index for beginning of current section */
			int indexOfSectionStart = 0;
			
			for(int i = 0; i<header.length; i++)
			{
				if (header[i] == DataBox.SEPARATOR_SECTION) // Found the separator
				{
//					System.out.println("Reached");
					switch (section)
					{
					case 0: // For header length bytes
						byte[] lengthBytes = Arrays.copyOfRange(header, indexOfSectionStart, i); // i-1 because we do not want separator been included
//						System.out.println(lengthBytes.length);
						headerByteArrayList.add(lengthBytes); // Add time bytes to return array list
						section++; // Move to the next section
						indexOfSectionStart = i+1; // Move the index to start of the next section
						
						break;
						
					case 1: // For time bytes
						byte[] timeBytes = Arrays.copyOfRange(header, indexOfSectionStart, i); // i-1 because we do not want separator been included
//						System.out.println(timeBytes.length);
//						System.out.println(timeBytes[timeBytes.length - 1]);
						headerByteArrayList.add(timeBytes); // Add time bytes to return array list
						section++; // Move to the next section
						indexOfSectionStart = i+1; // Move the index to start of the next section
						
						break;
						
						/* Here may add more components of the header */
						
						default:
							break;
					} // end switch
				} // end if
			} // end for
			
			/* Return the header components */
			return headerByteArrayList;
		}
		else // Failed to get the length value
		{
			return null; /* Return null */
		}
		
	}
	
	/**
	 * Read the header length from a boxed byte array
	 * @param original Boxed byte array
	 * @return The length of the header
	 */
	public static int getHeaderLength(byte[] bytesWithHeader)
	{
		/* Get the bytes contains length of the header */
		byte[] headerLengthByte = Arrays.copyOfRange(bytesWithHeader, 0, 2);
//		System.out.println(headerLengthByte.length);
		/* Convert length bytes to integer */
		int headerLength = IntFromDecimalAscii(headerLengthByte);
//		System.out.println(headerLength);
		/* Return an integer indicates the length of the header */
		return headerLength;
	}
	
	/**
	 * Parse date from a date string
	 * @param headerByteList An byte array list contains components of a header 
	 * @return date
	 */
	public static Date getDate(ArrayList<byte[]> headerByteList)
	{
		String dateString = null;
		try
		{
			/* Convert an byte array to string using coding UTF-8 */
			dateString = new String(headerByteList.get(DataBox.SECTION_NUMBER_DATE), "UTF-8");
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}

		Date date = null;
		try
		{
			/* Parse to get date from a string */
			date = DateSyncUtil.parse(dateString);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		
		return date;
	}
	
	/**
	 * One implementation for converting decimal numbers
	 * @author Jason Watkins
	 * @param bytes byte array need to be converted
	 * @return integer
	 */
	private static int IntFromDecimalAscii(byte[] bytes)
	{
	    int result = 0;

	    // For each digit, add the digit's value times 10^n, where n is the
	    // column number counting from right to left starting at 0.
	    for(int i = 0; i < bytes.length; ++i)
	    {
	        // ASCII digits are in the range 48 <= n <= 57. This code only
	        // makes sense if we are dealing exclusively with digits, so
	        // throw if we encounter a non-digit character
	        if(bytes[i] < 48 || bytes[i] > 57)
	        {
	            MainEntry.logger.log(Level.SEVERE, "Non-digit character present: " + bytes[i]);
	            return -1;
	        }

	        // The bytes are in order from most to least significant, so
	        // we need to reverse the index to get the right column number
	        int exp = bytes.length - i - 1;

	        // Digits in ASCII start with 0 at 48, and move sequentially
	        // to 9 at 57, so we can simply subtract 48 from a valid digit
	        // to get its numeric value
	        int digitValue = bytes[i] - 48;

	        // Finally, add the digit value times the column value to the
	        // result accumulator
	        result += digitValue * (int)Math.pow(10, exp);
	    }

	    return result;
	}
}
