package spider;

import java.util.Date;

import org.apache.james.mime4j.field.datetime.DateTime;

public class Users {

	public static String[] userNames;
	public static String[] passWords;
	public static Date[] usingtime;
	public static boolean[] isUsed;
	public static int userCount;
	
	public static String newUser()
	{
		String pair = ""; int idx = -1;
		Date date = new Date();
		date.getTime();
		for (int i = 0; i < userCount; i++)
			if (!isUsed[i] && usingtime[i].before(date)){
				date = usingtime[i];
				idx = i;
			}
		if (idx >= 0)
		{
			pair += userNames[idx] + "," + passWords[idx];
			isUsed[idx] = true;
		}
		return pair;
	}
}
