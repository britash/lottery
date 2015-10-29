package com.monk.lottery.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Hello world!
 *
 */
public class DateUtils {
	
	public static final String YYYYMMDDHHMMSS = "yyyyMMdd HH:mm:ss";
	private static SimpleDateFormat sdf = new SimpleDateFormat(YYYYMMDDHHMMSS);
	
	public static String dateToString(Date date){
		return date == null ? "" :sdf.format(date);
	}
	
	public static Date addsecond(Date date,int sencend){
		if(date == null){
			return date;
		}
		Calendar cal = Calendar.getInstance();
    	cal.setTime(date);
    	cal.add(Calendar.SECOND, sencend);
    	return cal.getTime();
	}
	
	public static Date setToDayStart(Date date){
		if(date == null){
			return date;
		}
		Calendar cal = Calendar.getInstance();
    	cal.setTime(date);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	return cal.getTime();
	}
	
	public static Integer getYear(Date date){
		if(date == null){
			date = new Date();
		}
		Calendar cal = Calendar.getInstance();
    	cal.setTime(date);
    	return cal.get(Calendar.YEAR);
	}
	
	
	public static boolean isBetween(Date startDate,Date endDate,Date currentDate){
		if(currentDate == null || startDate == null || endDate == null){
			return false;
		}
		return startDate.before(currentDate) && endDate.after(currentDate);
	}
	public static void main(String[] args) {
		Date date = new Date();
		Date beforedate = setToDayStart(date);
		Date afterDate = addsecond(date,10000);
		System.out.println(getYear(date));
	}
	
}
