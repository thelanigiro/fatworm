package threadpool;

public class ParserThreadPool extends ThreadGroup {


    /** Task Dispatcher thread associated with this threadpool. */
    //protected DispatcherThread dispatcherThread;

    /** Array of threads in the pool. */
    protected ParserThread[] pool;
    
    /** Scheduling ParserTask thread in the pool. */
    protected ParserScheduleThread parserScheduleThread;

    /** Size of the pool. */
    protected int poolSize;

    /**
     * Public constructor
     * @param poolName name of the threadPool
     * @param threadName name for the worker Threads
     * @param poolSize number of threads in the pool
     */
    public ParserThreadPool(String poolName, String threadName, int poolSize) {
        super(poolName);

        this.poolSize = poolSize;

        //dispatcherThread = new DispatcherThread(this, threadName + " dispatcher", this);
        pool = new ParserThread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            pool[i] = new ParserThread(this, threadName, i);
            synchronized (this) {
                try {
                    pool[i].start();
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        parserScheduleThread = new ParserScheduleThread(this, "ParserScheduleThread");
        synchronized (this) {
            try {
            	parserScheduleThread.start();
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Assigns a worker task to the pool.  The threadPool will select a worker
     * thread to execute the task.
     * @param task the WorkerTask to be executed.
     */
    public synchronized void assign(ParserTask task) {
        while (true) {
            for (int i = 0; i < poolSize; i++) {
                if (pool[i].isAvailable()) {
                    pool[i].assign(task);
                    return;
                }
            }
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Assigns a DispatcherTask to the threadPool.  The dispatcher thread
     * associated with the threadpool will execute it.
     * @param task DispatcherTask that will keep the workers busy
     */
    //public void assignGroupTask(DispatcherTask task) {
    //    dispatcherThread.assign(task);
    //}

    /**
     * Returns the percentage of worker threads that are busy.
     * @return int value representing the percentage of busy workers
     */
    public int getOccupation() {
        int occupied = 0;
        for (int i = 0; i < poolSize; i++) {
        	ParserThread thread = pool[i];
            if (thread.isOccupied()) {
                occupied++;
            }
        }
        return (occupied * 100) / poolSize;
    }

    public int getBlockedPercentage() {
        int counter = 0;
        for (int i = 0; i < poolSize; i++) {
        	ParserThread thread = pool[i];
            if (thread.getstate() == ParserThread.PARSERTHREAD_BLOCKED ) {
                counter++;
            }
        }
        return (counter * 100) / poolSize;
    }

    public int getBusyPercentage () {
        int counter = 0;
        for (int i = 0; i < poolSize; i++) {
        	ParserThread thread = pool[i];
            if (thread.getstate() == ParserThread.PARSERTHREAD_BUSY) {
                counter++;
            }
        }
        return (counter * 100) / poolSize;
    }

    public int getIdlePercentage ( ) {
        int counter = 0;
        for (int i = 0; i < poolSize; i++) {
        	ParserThread thread = pool[i];
            if (thread.getstate() == ParserThread.PARSERTHREAD_IDLE ) {
                counter++;
            }
        }
        return (counter * 100) / poolSize;
    }

    /**
     * Causes all worker threads to die.
     */
    public void stopAll() {
        for (int i = 0; i < pool.length; i++) {
        	ParserThread thread = pool[i];
            thread.stopRunning();
        }
    }

    /**
     * Returns the number of worker threads that are in the pool.
     * @return the number of worker threads in the pool
     */
    public int getSize ( ) {
        return poolSize;
    }

}
