package threadpool;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

public class BackupThread extends Thread {

	// public Schedule Schedule = ;

	public BackupThread(String name) {
		super(name);
	}

	public synchronized void run() {
		while (true) {

			Date time = new Date();
			File file = new File("backup_" + time.toString() + ".txt");
			FileOutputStream fout = null;
			try {
				fout = new FileOutputStream(file);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			DataOutputStream out = new DataOutputStream(fout);
			try {
				// synchronized (Schedule) {

				Integer count = Schedule.workertasks.size();
				out.write(count.toString().getBytes());
				out.write('\n');
				Integer num = Schedule.crawlNumber;
				out.write(num.toString().getBytes());
				out.write('\n');
				for (WorkerTask wk : Schedule.workertasks) {
					out.write(wk.id.getBytes());
					out.write('\n');
				}
				// }
				out.close();
				fout.close();

			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(10 * 60 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
