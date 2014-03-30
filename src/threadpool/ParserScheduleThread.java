package threadpool;

public class ParserScheduleThread extends Thread {
	
	protected ParserThreadPool stp;
	
	public ParserScheduleThread(ParserThreadPool stp, String name)
	{
		super(stp, name);
		this.stp = stp;
	}
	
	/**
     * Thread's overridden run method.
     */
    public synchronized void run() 
    {
    	synchronized (stp) {
            stp.notify();
        }
    	
        while (true) 
        {
            if (Schedule.getParserTaskSize() == 0)
            {
            	try
            	{
            		//System.out.println("[ParserScheduleThread] There is no parser task...");
            		Thread.sleep(50);
            	}
            	catch(InterruptedException e)
            	{
            		Thread.currentThread().interrupt();
            	}
            }
            else
            {
            	ParserTask parsertask = Schedule.removeParserTask();
        		if (parsertask != null)
        			stp.assign(parsertask);
            }            
        }
    }

}
