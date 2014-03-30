package threadpool;

/**
 * Thread Pool implementation that will be used for pooling the spider and
 * parser threads.
 * 
 * $Id: WorkerThreadPool.java,v 1.0 2013/10/27 16:47:49 $
 * 
 * @author Nannan, Wu
 */
public class WorkerThreadPool extends ThreadGroup {

	/** Task Dispatcher thread associated with this threadpool. */
	// protected DispatcherThread dispatcherThread;

	/** Array of threads in the pool. */
	protected WorkerThread[] pool;

	/** Scheduling WorkerTask thread in the pool. */
	protected WorkerScheduleThread workerScheduleThread;

	/** Size of the pool. */
	protected int poolSize;

	/**
	 * Public constructor
	 * 
	 * @param poolName
	 *            name of the threadPool
	 * @param threadName
	 *            name for the worker Threads
	 * @param poolSize
	 *            number of threads in the pool
	 */
	public WorkerThreadPool(String poolName, String threadName, int poolSize) {
		super(poolName);

		this.poolSize = poolSize;

		// dispatcherThread = new DispatcherThread(this, threadName +
		// " dispatcher", this);
		pool = new WorkerThread[poolSize];
		for (int i = 0; i < poolSize; i++) {
			pool[i] = new WorkerThread(this, threadName, i);
			synchronized (this) {
				try {
					pool[i].start();
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		workerScheduleThread = new WorkerScheduleThread(this,
				"WorkerScheduleThread");
		synchronized (this) {
			try {
				workerScheduleThread.start();
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Assigns a worker task to the pool. The threadPool will select a worker
	 * thread to execute the task.
	 * 
	 * @param task
	 *            the WorkerTask to be executed.
	 */
	public synchronized void assign(WorkerTask task) {
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
	 * Assigns a DispatcherTask to the threadPool. The dispatcher thread
	 * associated with the threadpool will execute it.
	 * 
	 * @param task
	 *            DispatcherTask that will keep the workers busy
	 */
	// public void assignGroupTask(DispatcherTask task) {
	// dispatcherThread.assign(task);
	// }

	/**
	 * Returns the percentage of worker threads that are busy.
	 * 
	 * @return int value representing the percentage of busy workers
	 */
	public int getOccupation() {
		int occupied = 0;
		for (int i = 0; i < poolSize; i++) {
			WorkerThread thread = pool[i];
			if (thread.isOccupied()) {
				occupied++;
			}
		}
		return (occupied * 100) / poolSize;
	}

	public int getBlockedPercentage() {
		int counter = 0;
		for (int i = 0; i < poolSize; i++) {
			WorkerThread thread = pool[i];
			if (thread.getstate() == WorkerThread.WORKERTHREAD_BLOCKED) {
				counter++;
			}
		}
		return (counter * 100) / poolSize;
	}

	public int getBusyPercentage() {
		int counter = 0;
		for (int i = 0; i < poolSize; i++) {
			WorkerThread thread = pool[i];
			if (thread.getstate() == WorkerThread.WORKERTHREAD_BUSY) {
				counter++;
			}
		}
		return (counter * 100) / poolSize;
	}

	public int getIdlePercentage() {
		int counter = 0;
		for (int i = 0; i < poolSize; i++) {
			WorkerThread thread = pool[i];
			if (thread.getstate() == WorkerThread.WORKERTHREAD_IDLE) {
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
			WorkerThread thread = pool[i];
			thread.stopRunning();
		}
	}

	/**
	 * Returns the number of worker threads that are in the pool.
	 * 
	 * @return the number of worker threads in the pool
	 */
	public int getSize() {
		return poolSize;
	}
	
	/**
	 * Returns the vector of worker threads that are in the pool.
	 * 
	 * @return the vector of worker threads in the pool
	 */
	public WorkerThread[] getPool() {
		return pool;
	}

}
