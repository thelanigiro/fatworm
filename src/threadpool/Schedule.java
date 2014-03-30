package threadpool;

import java.util.ArrayList;

import org.openqa.selenium.WebElement;

/**
 * Schedule crawling individuals.
 * 
 *
 * $Id: Schedule.java,v 1.0 2013/10/27 16:47:49 $
 *
 * @author Nannan, Wu
 */
public class Schedule {
	
	public static ArrayList<WorkerTask> workertasks
		= new ArrayList<WorkerTask>();
	public static ArrayList<ParserTask> parsertasks
		= new ArrayList<ParserTask>();
	
	//hasht
	
	public static int crawlNumber = 0;
	
	
	public Schedule()
	{
		
	}
	
	public static boolean addWorkerTask(String id)
	{
		boolean b = true;
		for (WorkerTask t : workertasks) {
			if (t.id.equals(id)){
				b = false;
				break;
			}
		}
		if (!b)
			return true;
		try
		{
			WorkerTask workertask = new WorkerTask(id);
			synchronized(workertasks)
			{				
				workertasks.add(workertask);
			}
			return true;
		}
		catch(Exception ex)
		{
			System.out.println("Schedule adding WorkerTask failed...");
			return false;
		}
	}
	
	public static boolean addWorkerTask(WorkerTask workertask)
	{
		boolean b = true;
		for (WorkerTask t : workertasks) {
			if (t.id.equals(workertask.id)){
				b = false;
				break;
			}
		}
		if (!b)
			return true;
		try
		{
			synchronized(workertasks)
			{
				workertasks.add(workertask);
			}
			return true;
		}
		catch(Exception ex)
		{
			System.out.println("Schedule adding WorkerTask failed...");
			return false;
		}
	}
	
	public static WorkerTask removeWorkerTask()
	{
		try
		{
			WorkerTask workertask = null;
			synchronized(workertasks)
			{		
				if (workertasks.size() > crawlNumber)
					workertask = workertasks.get(crawlNumber);
				System.out.println(crawlNumber);
				crawlNumber++;
				/*if (workertasks.size() - 1 >= 0)
					workertask = workertasks.remove(workertasks.size() - 1);
				else
					System.out.println("Schedule removing WorkerTask failed, as it is empty...");*/
				
			}
			return workertask;
		}
		catch(Exception ex)
		{
			System.out.println("Schedule removing WorkerTask failed, as it is empty...");
			return null;
		}
	}
	
	public static int getWorkerTaskSize()
	{
		int size = 0;
		synchronized(workertasks)
		{	
			size = workertasks.size();
		}
		return size;
	}
	
	public static void showWorkerTask()
	{
		for (int i = 0; i < workertasks.size(); i++)
			System.out.println(workertasks.get(i).id);
		System.out.println("The list has " + workertasks.size() + " items...");
	}
	
	
	public static boolean addParserTask(String html, String type)
	{
		try
		{
			ParserTask parsertask = new ParserTask(html, type);
			synchronized(parsertasks)
			{	
				parsertasks.add(parsertask);
			}
			return true;
		}
		catch(Exception ex)
		{
			System.out.println("Schedule adding ParserTask failed...");
			return false;
		}
	}
	
	public static boolean addParserTask(ParserTask parsertask)
	{
		try
		{
			synchronized(parsertasks)
			{	
				parsertasks.add(parsertask);
			}
			return true;
		}
		catch(Exception ex)
		{
			System.out.println("Schedule adding ParserTask failed...");
			return false;
		}
	}
	
	public static ParserTask removeParserTask()
	{
		try
		{
			ParserTask parsertask = null;
			synchronized(parsertasks)
			{					
				if (parsertasks.size() - 1 >= 0)
					parsertask = parsertasks.remove(parsertasks.size() - 1);
				else
					System.out.println("Schedule removing ParserTask failed, as it is empty...");
			}
			return parsertask;
		}
		catch(Exception ex)
		{
			System.out.println("Schedule removing ParserTask failed, as it is empty...");
			return null;
		}
	}
	
	public static int getParserTaskSize()
	{
		int size = 0;
		synchronized(parsertasks)
		{	
			size = parsertasks.size();
		}
		return size;
	}
	
	public static void showParserTask()
	{
		for (int i = 0; i < parsertasks.size(); i++)
			System.out.println(parsertasks.get(i).html);
		System.out.println("The list has " + parsertasks.size() + " items...");
	}

}
