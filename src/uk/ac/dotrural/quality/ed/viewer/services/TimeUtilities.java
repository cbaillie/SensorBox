package uk.ac.dotrural.quality.ed.viewer.services;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeUtilities {
	
	public static String parseTime(String timestamp)
	{	
		//String[] days = {"Sun","Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
		String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		StringBuilder sb = new StringBuilder();
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(Long.parseLong(timestamp));
		
		//sb.append(days[cal.get(Calendar.DAY_OF_WEEK)]);
		sb.append(parseDay(cal.get(Calendar.DAY_OF_WEEK)));
		sb.append(' ');
		sb.append(cal.get(Calendar.DATE));
		sb.append(' ');
		sb.append(months[cal.get(Calendar.MONTH)]);
		sb.append(" @ ");
		sb.append(cal.get(Calendar.HOUR_OF_DAY));
		sb.append(':');
		
		int mins = cal.get(Calendar.MINUTE);
		if(mins < 10)
			sb.append('0');
		sb.append(mins);
		sb.append(':');
		
		int secs = cal.get(Calendar.SECOND);
		if(secs < 10)
			sb.append('0');
		sb.append(cal.get(Calendar.SECOND));
		
		return sb.toString();
	}

	private static String parseDay(int day)
	{
		switch(day)
		{
		case Calendar.SUNDAY:
			return "Sun";
		case Calendar.MONDAY:
			return "Mon";
		case Calendar.TUESDAY:
			return "Tue";
		case Calendar.WEDNESDAY:
			return "Wen";
		case Calendar.THURSDAY:
			return "Thu";
		case Calendar.FRIDAY:
			return "Fri";
		case Calendar.SATURDAY:
			return "Sat";
		}
		return "";
	}
	
}
