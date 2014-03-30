package spider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import threadpool.BackupThread;
import threadpool.MonitorThread;
import threadpool.ParserThreadPool;
import threadpool.Schedule;
import threadpool.WorkerTask;
import threadpool.WorkerThreadPool;

public class Spider {

	public static WorkerThreadPool wtpool;
	public static ParserThreadPool ptpool;
	public static MonitorThread monitorThread;
	public static BackupThread backupThread;

	public static Facebook[] facebooks;

	// private static final int WORKER_THREAD_COUNT = 5;
	// private static final int USER_PER_THREAD = 8;

	/** accounts of facebook */
	public static String username[];
	public static String password[];
	public static Integer loopnumber[];

	public Spider() {

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		//restore("/home/wddwxy/文档/facebookSpider/fbspider/backup_Thu Jan 16 19:19:04 CST 2014.txt");    //恢复
		crawle();
		fbSpider();

		while (!wtpool.isDestroyed() || !ptpool.isDestroyed()) {
			try {
				// ;
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	public static void fbSpider() {
		int user_per_thread = 2;
		int workerThreadCount = 1;
		int parserThreadcount = 1;

		String[][] userNames = new String[workerThreadCount][user_per_thread];
		String[][] passWords = new String[workerThreadCount][user_per_thread];
		Integer[][] loopnumber = new Integer[workerThreadCount][user_per_thread];

		FileReader fr;
		try {
			fr = new FileReader("login_users.txt");
			BufferedReader br = new BufferedReader(fr);

			for (int i = 0; i < workerThreadCount; i++) {
				for (int j = 0; j < user_per_thread; j++) {
					String s;
					try {
						s = br.readLine();
						while (s.equals("")) {
							s = br.readLine();
						}
						String[] userInfo = s.split("\\|");
						userNames[i][j] = userInfo[0];
						passWords[i][j] = userInfo[1];
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

		/*
		 * username= new String[12]; username[0] = "wddwxy2008@gmail.com";
		 * username[1] = "doctorwunannan@gmail.com"; username[2] =
		 * "wddwxy2008@gmail.com"; username[3] = "doctorwunannan@gmail.com";
		 * username[4] = "wddwxy2008@gmail.com"; username[6] =
		 * "doctorwunannan@gmail.com"; username[5] = "wddwxy2008@gmail.com";
		 * username[7] = "doctorwunannan@gmail.com"; username[8] =
		 * "wddwxy2008@gmail.com"; username[10] = "doctorwunannan@gmail.com";
		 * username[9] = "wddwxy2008@gmail.com"; username[11] =
		 * "doctorwunannan@gmail.com"; password = new String[12]; password[0] =
		 * "wdd2004"; password[1] = "218716"; password[2] = "wdd2004";
		 * password[3] = "218716"; password[4] = "wdd2004"; password[6] =
		 * "218716"; password[5] = "wdd2004"; password[7] = "218716";
		 * password[8] = "wdd2004"; password[10] = "218716"; password[9] =
		 * "wdd2004"; password[11] = "218716";
		 */

		facebooks = new Facebook[workerThreadCount];
		for (int i = 0; i < workerThreadCount; i++) {
			// facebooks[i] = new Facebook(username[i], password[i]);

			String[] temp1 = new String[user_per_thread];
			// temp1[0] = userNames[i][0]; //temp1[1] = userNames[i][1];
			String[] temp2 = new String[user_per_thread];
			// temp2[0] = passWords[i][0]; //temp2[1] = passWords[i][1];
			for (int j = 0; j < user_per_thread; j++) {
				temp1[j] = userNames[i][j];
				temp2[j] = passWords[i][j];
			}

			facebooks[i] = new Facebook(userNames[i][0], passWords[i][0]);
			facebooks[i].curUser = 0;

			facebooks[i].userNames = temp1;
			facebooks[i].passWords = temp2;
			facebooks[i].userCount = user_per_thread;
			facebooks[i].loop = 1;
		}

		
		//crawle();

		System.out.println("\nMult worker Threads running...\n");

		monitorThread = new MonitorThread("MonitorThread");
		monitorThread.start();

		wtpool = new WorkerThreadPool("workerpool", "worker", workerThreadCount);
		ptpool = new ParserThreadPool("parserpool", "parser", parserThreadcount);

		monitorThread.workerThreadPool = wtpool;
		monitorThread.parserThreadPool = ptpool;

		backupThread = new BackupThread("BackupThread");
		backupThread.start();

		// backupThread.schedule =
	}

	public static void crawle() {
		String[] works = { "erikalwhite", "Sr.Diego.of.Empire.Academy",
				"marialuizapessoa.pessoa", "NBA2K", "TomHanks", "McDonalds",
				"UCBerkeley" };
		for (int i = 0; i < 7; i++) {
			Schedule.addWorkerTask(works[i]);
		}
		Schedule.showWorkerTask();
	}

	public static void restore(String loc) {
		FileReader fr;
		String s;
		try {
			fr = new FileReader(loc);
			BufferedReader br = new BufferedReader(fr);
			int count = Integer.parseInt(br.readLine());
			Schedule.crawlNumber = Integer.parseInt(br.readLine());

			for (int i = 0; i < count; i++) {

				try {
					s = br.readLine();
					while (s.equals("")) {
						s = br.readLine();
					}
					Schedule.addWorkerTask(s);
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}
