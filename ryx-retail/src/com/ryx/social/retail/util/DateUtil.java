package com.ryx.social.retail.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtil {
	
	private static final SimpleDateFormat compactDateFormat = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat compactTimeFormat = new SimpleDateFormat("HHmmss");
	private static final SimpleDateFormat compactDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final SimpleDateFormat compactMonthFormat = new SimpleDateFormat("yyyyMM");

	private static final SimpleDateFormat standardDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat standardDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static final long sevenDaysMillis = 7*24*3600*1000L;
	public static final long thirtyDaysMillis = 30*24*3600*1000L;
	
	public static Date getNow(boolean ignoreMillis) {
		if(ignoreMillis) {
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(now.getTimeInMillis()/1000*1000);
			return now.getTime();
		} else {
			return Calendar.getInstance().getTime();
		}
	}
	
	public static Date getNow() {
		return getNow(false);
	}
	
	public static Date getToday() {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(now.getTimeInMillis()/1000*1000);
		now.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE), 0, 0, 0);
		return now.getTime();
	}
	
	public static Date getTomorrow(Date originalDate) {
		if(originalDate==null) return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(originalDate);
		calendar.add(Calendar.DATE, 1);
		return calendar.getTime();
	}
	
	public static Date getCurrentMonth() {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(now.getTimeInMillis()/1000*1000);
		now.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 0, 0, 0, 0);
		return now.getTime();
	}
	
	public static Date getMonth(Date originalDate) {
		if(originalDate==null) return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(originalDate);
		calendar.setTimeInMillis(calendar.getTimeInMillis()/1000*1000);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 0, 0, 0, 0);
		return calendar.getTime();
	}
	
	public static Date getDay(Date originalDate) {
		if(originalDate==null) return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(originalDate);
		calendar.setTimeInMillis(calendar.getTimeInMillis()/1000*1000);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		return calendar.getTime();
	}
	
	public static Date parseDate(int year, int month, int day, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month-1, day, hour, minute, second);
		return calendar.getTime();
	}
	
	public static Date parseDate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month-1, day);
		return calendar.getTime();
	}
	
	public static Date parseDate(long millis) {
		return parseDate(millis, false);
	}
	
	public static Date parseDate(long millis, boolean ignoreMillis) {
		Calendar calendar = Calendar.getInstance();
		if(ignoreMillis) {
			millis = millis/1000*1000;
		}
		calendar.setTimeInMillis(millis);
		return calendar.getTime();
	}
	
	public static Date getLastMonth(Date originalDate, int previous) {
		if(originalDate==null) return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(originalDate);
		calendar.add(Calendar.MONTH, -1*previous);
		return calendar.getTime();
	}
	
	public static Date getLastMonth(Date originalDate) {
		if(originalDate==null) return null;
		return getLastMonth(originalDate, 1);
	}
	
	public static Date getCompactDate(String originalDateString) {
		return getCompactDate(originalDateString, null);
	}
	
	public static Date getCompactDate(String originalDateString, Date defaultValue) {
		try {
			return compactDateFormat.parse(originalDateString);
		} catch (ParseException e) {
			return defaultValue;
		}
	}
	
	public static Date getCompactDateTime(String originalDateString) {
		return getCompactDateTime(originalDateString, null);
	}
	
	public static Date getCompactDateTime(String originalDateString, Date defaultValue) {
		try {
			return compactDateTimeFormat.parse(originalDateString);
		} catch (ParseException e) {
			return defaultValue;
		}
	}
	
	public static Date getCompactMonth(String originalMonthString) {
		return getCompactMonth(originalMonthString, null);
	}
	
	public static Date getCompactMonth(String originalMonthString, Date defaultValue) {
		try {
			return compactMonthFormat.parse(originalMonthString);
		} catch (ParseException e) {
			return defaultValue;
		}
	}
	
	public static String getCompactDateString(Date originalDate) {
		return compactDateFormat.format(originalDate);
	}
	
	public static String getCompactTimeString(Date originalDate) {
		return compactTimeFormat.format(originalDate);
	}
	
	public static String getCompactDateTimeString(Date originalDate) {
		return compactDateTimeFormat.format(originalDate);
	}
	
	public static String getCompactMonthString(Date originalDate) {
		return compactMonthFormat.format(originalDate);
	}
	
	public static String getStandardDateString(Date originalDate) {
		return standardDateFormat.format(originalDate);
	}
	
	public static String getStandardDateTimeString(Date originalDate) {
		return standardDateTimeFormat.format(originalDate);
	}
	
	public static Date plus(Date originalDate, long addend) {
		while(addend>Integer.MAX_VALUE) {
			addend -= Integer.MAX_VALUE;
			originalDate = plus(originalDate, Integer.MAX_VALUE);
		}
		while(addend<Integer.MIN_VALUE) {
			addend -= Integer.MIN_VALUE;
			originalDate = plus(originalDate, Integer.MIN_VALUE);
		}
		return plus(originalDate, (int) addend);
	}
	
	public static Date plus(Date originalDate, int addend) {
		if(addend==0) {
			return originalDate;
		}
		Calendar originalCal = Calendar.getInstance();
		originalCal.setTime(originalDate);
		originalCal.add(Calendar.MILLISECOND, addend);
		return originalCal.getTime();
	}

	public static Date minus(Date originalDate, long subtrahend) {
		return plus(originalDate, -1L * subtrahend);
	}

	public static Date minus(Date originalDate, int subtrahend) {
		return plus(originalDate, -1 * subtrahend);
	}
	
	public static long minus(Date subtrahend) {
		return minus(subtrahend, false);
	}
	
	public static long minus(Date subtrahend, boolean ignoreMillis) {
		Calendar subtrahendCal = Calendar.getInstance();
		subtrahendCal.setTime(subtrahend);
		long difference = Calendar.getInstance().getTimeInMillis() - subtrahendCal.getTimeInMillis();
		if(ignoreMillis) {
			return difference/1000*1000;
		} else {
			return difference;
		}
	}
	
	public static long minus(Date minuend, Date subtrahend) {
		return minus(minuend, subtrahend, false);
	}
	
	public static long minus(Date minuend, Date subtrahend, boolean ignoreMillis) {
		Calendar minuendCal = Calendar.getInstance();
		minuendCal.setTime(minuend);
		Calendar subtrahendCal = Calendar.getInstance();
		subtrahendCal.setTime(subtrahend);
		long difference = minuendCal.getTimeInMillis() - subtrahendCal.getTimeInMillis();
		if(ignoreMillis) {
			return difference/1000*1000;
		} else {
			return difference;
		}
	}
	
	public static boolean isFirstDayOfMonth(Date date) {
		if(date==null) return false;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH)==1;
	}
	
	public static boolean isLastDayOfMonth(Date date) {
		if(date==null) return false;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH)==calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	public static boolean isEarlierThan(Date oneDate, Date otherDate) {
		return oneDate.before(otherDate);
	}
	
	public static boolean isNotEarlierThan(Date oneDate, Date otherDate) {
		return !oneDate.before(otherDate);
	}
	
	public static boolean isLaterThan(Date oneDate, Date otherDate) {
		return oneDate.after(otherDate);
	}
	
	public static boolean isNotLaterThan(Date oneDate, Date otherDate) {
		return !oneDate.after(otherDate);
	}
	
}
