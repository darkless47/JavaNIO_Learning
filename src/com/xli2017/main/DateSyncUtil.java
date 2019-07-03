package com.xli2017.main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Thread-safe date utility class
 * @author peida https://www.cnblogs.com/peida/
 *
 */
public class DateSyncUtil
{
	/**
	 * Simple date format "yyyy-MM-dd HH:mm:ss.SSS", and use Locale.US to make sure the language is english
	 */
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    
	/**
	 * The method to format a given date
	 * @param date The date need to be formatted
	 * @return String of the formatted date
	 * @throws ParseException 
	 */
    public static String formatDate(Date date) throws ParseException
    {
        synchronized(sdf)
        {
            return sdf.format(date);
        }  
    }
    
    /**
     * The method to parse a string that may contain a date
     * @param strDate A string that may contain a date
     * @return Parsed date
     * @throws ParseException
     */
    public static Date parse(String strDate) throws ParseException
    {
        synchronized(sdf)
        {
            return sdf.parse(strDate);
        }
    } 
}
