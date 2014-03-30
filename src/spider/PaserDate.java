package spider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaserDate {
	
	private Map<String, Integer> monthE2N = new HashMap<String, Integer>();
	private DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public PaserDate(){
		monthE2N.put("January", 1);
		monthE2N.put("February", 2);
		monthE2N.put("March", 3);
		monthE2N.put("April", 4);
		monthE2N.put("May", 5);
		monthE2N.put("June", 6);
		monthE2N.put("July", 7);
		monthE2N.put("August", 8);
		monthE2N.put("September", 9);
		monthE2N.put("October", 10);
		monthE2N.put("November", 11);
		monthE2N.put("December", 12);
	}
	
	public Date getDate(String dates){
		Date date = null;
		String [] d = dates.split(",");
		String [] monDay = d[1].split(" ");
		String [] yearTime = d[2].split(" ");
		int month = 0;
		int day = 0;
		int hour = 0;
		int year = 0;	
		int minute = 0;
		
		month = monthE2N.get(monDay[1]);
		day = Integer.parseInt(monDay[2]);
		year = Integer.parseInt(yearTime[1]);
		
		int len = yearTime[3].length();
		String time = yearTime[3].substring(0,len - 2);
		String id = yearTime[3].substring(len - 2);
		int i = time.indexOf(':');
		hour = Integer.parseInt(time.substring(0,i));
		minute = Integer.parseInt(time.substring(i+1));
		if (id.equals("pm"))
			hour += 12;
		
		String str = year + "-" + month + "-" + day + " " + hour + ":" + minute;
		//Date date1 = null;
		try {
			date = format1.parse(str);
			//date1 = format1.parse("2013-12-1 0:0");
		} catch (Exception e) {
			// TODO: handle exception
		}
//		Date date3 = new Date(113, month, day, hour, minute);
//		System.out.println(date.toString());
//		System.out.println(date3.toString());
//		System.out.println(date1.after(date));
//		
//		System.out.println(hour);
//		System.out.println(minute);
		
		return date;
	}
	
	public static void main(String arg[]){
		String d = "Wednesday, September 11, 2013 at 8:05pm";
		PaserDate p = new PaserDate();
		Date date = p.getDate(d);
	}

}
