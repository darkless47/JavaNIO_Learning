package com.xli2017.main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thread-safe date utility class
 * @author peida https://www.cnblogs.com/peida/
 *
 */
public class DateSyncUtil
{
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    public static String formatDate(Date date) throws ParseException
    {
        synchronized(sdf)
        {
            return sdf.format(date);
        }  
    }
    
    public static Date parse(String strDate) throws ParseException
    {
        synchronized(sdf)
        {
            return sdf.parse(strDate);
        }
    } 
}
