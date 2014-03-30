package threadpool;

import spider.Facebook;
import spider.Spider;


/**
 * Implementation of  a Worker Thread.
 * This thread will accept WorkerTasks and execute them.
 *
 * $Id: WorkerThreadPool.java,v 1.0 2013/10/27 16:47:49 $
 *
 * @author Nannan, Wu
 */
public class WorkerThread extends Thread {

    public static final int WORKERTHREAD_IDLE = 0;
    public static final int WORKERTHREAD_BLOCKED = 1;
    public static final int WORKERTHREAD_BUSY = 2;


    /** the current state of this thread - idle, blocked, or busy. */
    protected int state;

    /** Whether this instance is assigned a task. */
    protected boolean assigned;

    /** Whether we should keep alive this thread. */
    protected boolean running;

    /** Threadpool this worker is part of. */
    protected WorkerThreadPool stp;

    /** Task this worker is assigned to. */
    protected WorkerTask task;
    
    /** facebook agent for crawling data */
    protected Facebook facebook;

    /**
     * Public constructor.
     * @param stp thread pool this worker is part of
     * @param name name of the thread
     * @param i index in the pool
     */
    public WorkerThread(WorkerThreadPool stp, String name, int i) {
        super(stp, name + " " + i);
        this.stp = stp;
        running = false;
        assigned = false;
        state = WORKERTHREAD_IDLE;
        
        facebook = Spider.facebooks[i];
    }
    
    /**
     * Public constructor.
     * @param stp thread pool this worker is part of
     * @param name name of the thread
     * @param i index in the pool
     */
    public WorkerThread(WorkerThreadPool stp, String name, int i, String username, String password) {
        super(stp, name + " " + i);
        this.stp = stp;
        running = false;
        assigned = false;
        state = WORKERTHREAD_IDLE;
        
        facebook = new Facebook(username, password);
    }

    /**
     * Tests whether this worker thread instance can be assigned a task.
     * @return whether we're capable of handling a task.
     */
    public boolean isAvailable() {
        return (!assigned) && running;
    }

    /**
     * Determines whether we're occupied.
     * @return boolean value representing our occupation
     */
    public boolean isOccupied() {
        return assigned;
    }

    /**
     * Method that allows the threadPool to assign the worker a Task.
     * @param task WorkerTask to be executed.
     */
    public synchronized void assign(WorkerTask task) {
        if ( !running ) {
            //SHOULDN'T HAPPEN WITHOUT BUGS
            throw new RuntimeException("THREAD NOT RUNNING, CANNOT ASSIGN TASK !!!");
        }
        if (assigned) {
            //SHOULDN'T HAPPEN WITHOUT BUGS
            throw new RuntimeException("THREAD ALREADY ASSIGNED !!!");
        }
        this.task = task;
        assigned = true;
        notify();
    }

    /**
     * Tells this thread not to accept any new tasks.
     */
    public synchronized void stopRunning() {
        if ( ! running ) {
            throw new RuntimeException ("THREAD NOT RUNNING - CANNOT STOP !");
        }
        if ( assigned ) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        running = false;
        notify();
    }

    /**
     * Returns the state of this worker thread (idle, blocked or busy).
     * @return
     */
    public int getstate ( ) {
        return state;
    }

    /**
     * Thread's overridden run method.
     */
    public synchronized void run() {
        running = true;

        //Log log = LogFactory.getLog(WorkerThread.class);
        //log.debug("Worker thread (" + this.getName() + ") born");

        synchronized (stp) {
            stp.notify();
        }

        while (running) {
            if (assigned) {
                //state = WORKERTHREAD_BLOCKED;
                //task.prepare();
                state = WORKERTHREAD_BUSY;
                try {
                    //task.execute();
                    //task.tearDown();
                	
                	//Crawling code
                	while (!facebook.ready)
                		Thread.sleep(1000);
                	/*
                	System.out.println(Thread.currentThread().getName() + " is running...");
                	System.out.println("Username:" + facebook.userNames[facebook.curUser] +
                			" is used to crawl Facebook...");
                			*/
                	facebook.getTimeline(task.id);
                	facebook.getFriends(task.id);
                	
                } catch (Exception e) {
                    //log.fatal("PANIC! Task " + task + " threw an excpetion!", e);
                    //System.exit(1);
                		System.out.println(Thread.currentThread().getName() + 
                				" throw exception " + 
                				e.getMessage());
                	}

	            synchronized (stp) {
	                assigned = false;
	                task = null;
	                state = WORKERTHREAD_IDLE;
	                stp.notify();
	                this.notify(); // if some thread is blocked in stopRunning();
	            }
            }
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /* notify the thread pool that we died. */
        //log.debug("Worker thread (" + this.getName() + ") dying");
    }

	
}
