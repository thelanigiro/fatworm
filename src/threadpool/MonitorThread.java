package threadpool;

import java.util.Date;
import java.util.Random;

public class MonitorThread extends Thread {
	
	public WorkerThreadPool workerThreadPool = null;
	public ParserThreadPool parserThreadPool = null;
	public Date clock;
	
	public MonitorThread(String name)
	{
		super(name);
	}
	
	public synchronized void run() {
		
		clock = new Date();
		System.out.println("\nMonitor begin at " + clock.toString());
		while (true)
		{
			if (workerThreadPool != null)
			{
				System.out.println("\n============Monitor Info============");
				System.out.println("WorkerThreads Info...");
				System.out.print("size  :" + workerThreadPool.getSize());
				System.out.println("    \tbusy  :" + workerThreadPool.getBusyPercentage());
				System.out.print("occupy:" + workerThreadPool.getOccupation());
				System.out.println("    \tidle  :" + workerThreadPool.getIdlePercentage());
				System.out.println("block :" + workerThreadPool.getBlockedPercentage());	
			}			
			System.out.println("Worker Task Info...");
			System.out.println("worker tasks waiting for processing:" + Schedule.getWorkerTaskSize());
			//Schedule.showWorkerTask();
			System.out.println();
			System.out.println("Crawled person number:" + Schedule.crawlNumber);
			
			//reboot facebook
			Date now = new Date(); boolean rebootstatus;
			System.out.println("Now:" + now.toString());
			long diffMinutes;
			long diff;
			try{			
				WorkerThread[] pool = workerThreadPool.getPool();        
				for (int i = 0; i < workerThreadPool.getSize(); i++)
				{
					if (pool == null) continue;//?
					diffMinutes = (now.getTime() - pool[i].facebook.bornDate.getTime()) /
						(60 * 1000);
					diff = (now.getTime() - clock.getTime()) /(60 * 1000);
					System.out.println("Username:" + 
							pool[i].facebook.userNames[pool[i].facebook.curUser] + " born in " +
							pool[i].facebook.bornDate.toString() +
							" timespan " + diffMinutes);	
					if(diff > 210)
					{Thread.sleep(3600000);}
					Random ran = new Random();
					int span = ran.nextInt(10) + 25;
					if (diffMinutes > span){
						if(pool[i].facebook.loop % 16 != 0)
						{
							rebootstatus = pool[i].facebook.reboot(true,false);
						}else
						{
							rebootstatus = pool[i].facebook.reboot(true,true);
						}
						System.out.println("Reboot " + pool[i].getName() + " " +
								rebootstatus);
						//System.out.println("25min,no reboot");
					}
				}
			}catch(Exception ex){
				System.out.println(ex.getMessage());
			}
				
			/*
			if (parserThreadPool != null)
			{
				System.out.print("\nParserThreads Info...");
				System.out.println("    \tsize  :" + parserThreadPool.getSize());
				System.out.print("busy  :" + parserThreadPool.getBusyPercentage());
				System.out.println("    \toccupy:" + parserThreadPool.getOccupation());
				System.out.print("idle  :" + parserThreadPool.getIdlePercentage());
				System.out.println("    \tblock :" + parserThreadPool.getBlockedPercentage());
			}
			
			System.out.println("Parser Task Info...");
			System.out.println("parser tasks waiting for processing:" + Schedule.getParserTaskSize());					
			System.out.println("====================================\n");
			*/
			try
			{
				Thread.sleep(3000);
			}catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
			
			
		}
	
	}

}
