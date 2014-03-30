package threadpool;

public class WorkerScheduleThread extends Thread {

	protected WorkerThreadPool stp;

	public WorkerScheduleThread(WorkerThreadPool stp, String name) {
		super(stp, name);
		this.stp = stp;
	}

	/**
	 * Thread's overridden run method.
	 */
	public synchronized void run() {

		synchronized (stp) {
			stp.notify();
		}

		while (true) {
			if (Schedule.getWorkerTaskSize() == Schedule.crawlNumber) {
				try {
					// System.out.println("[WorkerScheduleThread] There is no worker task...");
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} else {
				WorkerTask workertask = Schedule.removeWorkerTask();
				if (workertask != null)
					stp.assign(workertask);
			}
		}
	}
}
