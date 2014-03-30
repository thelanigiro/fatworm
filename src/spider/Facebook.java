package spider;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import threadpool.Schedule;

public class Facebook {
	private int maxLoadTime = 120;

	private String driverPath = "chromedriver"; // ����"D:\\chromedriver.exe"Ϊchrome�����ַ
	private WebDriver webDriver;
	private Date limitDate = new Date(113, 11, 1, 0, 0);
	private PaserDate pd = new PaserDate();

	// user
	public String[] userNames;
	public String[] passWords;
	public int userCount;
	public int curUser;
	public Date bornDate;
	public int loop;

	// If ready is true, then the object can be used to crawl data.
	public boolean ready;

	protected void get(String url, int actionCount) {
		boolean inited = false;
		int index = 0, timeout = 10;
		while (!inited && index < actionCount) {
			timeout = (index == actionCount - 1) ? maxLoadTime : 10;// 最后一次跳转使用最大的默认超时时间
			inited = navigateAndLoad(url, timeout);
			index++;
		}
		if (!inited && index == actionCount) {// 最终跳转失败则抛出运行时异常，退出运行
			throw new RuntimeException("can not get the url [" + url
					+ "] after retry " + actionCount + "times!");
		}
	}

	/**
	 * rewrite the get method, adding user defined log</BR>
	 * 地址跳转方法，使用WebDriver原生get方法，默认加载超重试【1】次。
	 * 
	 * @param url
	 *            the url you want to open.
	 * @throws RuntimeException
	 */
	protected void get(String url) {
		get(url, 2);
	}

	/**
	 * judge if the url has navigate and page load completed.</BR>
	 * 跳转到指定的URL并且返回是否跳转完整的结果。
	 * 
	 * @param url
	 *            the url you want to open.
	 * @param timeout
	 *            the timeout for page load in seconds.
	 * @return if page load completed.
	 */
	private boolean navigateAndLoad(String url, int timeout) {
		try {
			webDriver.manage().timeouts()
					.pageLoadTimeout(timeout, TimeUnit.SECONDS);
			webDriver.get(url);
			return true;// 跳转并且加载页面成功在返回true
		} catch (TimeoutException e) {
			return false;// 超时的情况下返回false
		} catch (Exception e) {
			// failValidation();//共用的异常处理方法
			// LOG.error(e);//记录错误日志
			throw new RuntimeException(e);// 抛出运行时异常，退出运行
		} finally {
			webDriver.manage().timeouts()
					.pageLoadTimeout(maxLoadTime, TimeUnit.SECONDS);
		}
	}

	public Facebook() {
		ready = false;
		String userName = "wddwxy2008@gmail.com";
		String passWord = "wdd2004";

		FirefoxProfile firefoxProfile = new FirefoxProfile();
		// Disable CSS
		// firefoxProfile.setPreference("permissions.default.stylesheet", 2);
		// Disable images
		firefoxProfile.setPreference("permissions.default.image", 2);
		// Disable Flash
		firefoxProfile.setPreference(
				"dom.ipc.plugins.enabled.libflashplayer.so", "false");

		// System.getProperties().setProperty("webdriver.chrome.driver",driverPath);
		webDriver = new FirefoxDriver(firefoxProfile);
		// webDriver = new HtmlUnitDriver(true);
		// webDriver.manage().timeouts().pageLoadTimeout(1, TimeUnit.SECONDS);
		/**
		 * ��½facebook
		 */
		get("https://www.facebook.com/");
		WebElement username = webDriver.findElement(By.id("email"));
		WebElement pass = webDriver.findElement(By.id("pass"));
		WebElement form = webDriver.findElement(By.id("login_form"));
		username.sendKeys(userName);
		pass.sendKeys(passWord);
		form.submit();
		ready = true;
		curUser = 0;
		bornDate = new Date();

	}

	public Facebook(String userName, String passWord) {
		ready = false;
		// System.getProperties().setProperty("webdriver.chrome.driver",driverPath);
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		// Disable CSS
		// firefoxProfile.setPreference("permissions.default.stylesheet", 2);
		// Disable images
		firefoxProfile.setPreference("permissions.default.image", 2);
		// Disable Flash
		firefoxProfile.setPreference(
				"dom.ipc.plugins.enabled.libflashplayer.so", "false");

		// System.getProperties().setProperty("webdriver.chrome.driver",driverPath);
		webDriver = new FirefoxDriver(firefoxProfile);
		// webDriver = new HtmlUnitDriver(true);
		// webDriver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
		/**
		 * ��½facebook
		 */
		try {
			get("https://www.facebook.com/");
			WebElement username = webDriver.findElement(By.id("email"));
			WebElement pass = webDriver.findElement(By.id("pass"));
			WebElement form = webDriver.findElement(By.id("login_form"));
			username.sendKeys(userName);
			pass.sendKeys(passWord);
			Thread.sleep(5000);// ?
			form.submit();
		} catch (Exception e) {
			// TODO: handle exception
			get("https://www.facebook.com/");
			WebElement username = webDriver.findElement(By.id("email"));
			WebElement pass = webDriver.findElement(By.id("pass"));
			WebElement form = webDriver.findElement(By.id("login_form"));
			// Thread.sleep(5000);
			username.sendKeys(userName);
			pass.sendKeys(passWord);
			form.submit();
		}
		ready = true;
		curUser = 0;
		bornDate = new Date();
	}

	public boolean reboot(boolean isReboot, boolean next) {
		if (!isReboot)
			return false;

		ready = false;

		String userName = "";
		String passWord = "";
		if (next) {
			curUser = (curUser + 1) % userCount;
			// loop = 0;
		}

		userName = userNames[curUser];
		passWord = passWords[curUser];
		// loopnumber[curUser]++;

		if (webDriver != null)
			webDriver.quit();

		// System.getProperties().setProperty("webdriver.chrome.driver",driverPath);
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		// Disable CSS
		// firefoxProfile.setPreference("permissions.default.stylesheet", 2);
		// Disable images
		firefoxProfile.setPreference("permissions.default.image", 2);
		// Disable Flash
		firefoxProfile.setPreference(
				"dom.ipc.plugins.enabled.libflashplayer.so", "false");

		// System.getProperties().setProperty("webdriver.chrome.driver",driverPath);
		webDriver = new FirefoxDriver(firefoxProfile);
		// webDriver = new HtmlUnitDriver(true);
		// webDriver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
		/**
		 * ��½facebook
		 */
		try {
			get("https://www.facebook.com/");
			WebElement username = webDriver.findElement(By.id("email"));
			WebElement pass = webDriver.findElement(By.id("pass"));
			WebElement form = webDriver.findElement(By.id("login_form"));
			username.sendKeys(userName);
			pass.sendKeys(passWord);
			Thread.sleep(5000);
			form.submit();
		} catch (Exception e) {
			// TODO: handle exception
			get("https://www.facebook.com/");
			WebElement username = webDriver.findElement(By.id("email"));
			WebElement pass = webDriver.findElement(By.id("pass"));
			WebElement form = webDriver.findElement(By.id("login_form"));
			// Thread.sleep(5000);
			username.sendKeys(userName);
			pass.sendKeys(passWord);
			form.submit();
		}
		ready = true;
		bornDate = new Date();
		loop = loop + 1;
		return true;
	}

	/**
	 * 
	 * @param webDriver
	 * @param personID
	 *            ����ȡ�˵���ַ
	 * @param MAXTIMELINE
	 *            �����ȡ��
	 */
	public void getTimeline(String uid) {
		int timelineCount = 0;
		// config
		int MAXTIMELINE = 50;
		String timeline = "https://www.facebook.com/" + uid + "/timeline/2013";
		String setscroll = "scroll(0,200000)";
		String contentClassName = "_3rbf";
		String timelineUnitClassName = "fbTimelineUnit";
		String contentClassName2 = "fbTimelineCapsule";
		String createTimeClassName = "_1_n";
		double span = 0;

		try {
			//Thread.sleep(1000);
			get(timeline);
		} catch (Exception e) {
			// TODO: handle exception
			return;
		}

		JavascriptExecutor jse = (JavascriptExecutor) webDriver;
		WebElement timelineList = null;
		try {
			timelineList = webDriver
					.findElement(By.className(contentClassName));
		} catch (Exception e) {
			// TODO: handle exception
			try {
				timelineList = webDriver.findElement(By
						.className(contentClassName2));
			} catch (Exception e1) {
				// TODO: handle exception
			}
		}

		List<WebElement> list = null;
		try {
			list = timelineList.findElements(By
					.className(timelineUnitClassName));
			timelineCount = list.size();
		} catch (Exception e) {
			// TODO: handle exception
		}

		boolean b = true;

		while (b) {
			span = Math.random()*8000 + 500;
			
			for (int i = 0; i < timelineCount; i++) {
				try {
					String time = list.get(i)
							.findElement(By.className(createTimeClassName))
							.findElement(By.tagName("abbr"))
							.getAttribute("title");
					Date date = pd.getDate(time);
					if (limitDate.after(date)) {
						b = false;
						list.remove(i);
						i--;
						timelineCount--;
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			if (!b)
				break;
			jse.executeScript(setscroll);
			try {
				//Thread.sleep(5000);
				Thread.sleep((long)span);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				list = timelineList.findElements(By
						.className(timelineUnitClassName));
			} catch (Exception e) {
				// TODO: handle exception
			}
			int count = list.size();
			if (count == timelineCount)
				break;
			else
				timelineCount = count;
		}

		for (int i = 0; i < timelineCount; i++) {
			span = Math.random()*8000 + 500;
			
			System.out.print("Crawling " + i + " timeline of " + uid);
			int commentClickNum = 0;
			try {
				try {
					WebElement showComment = list.get(i).findElement(
							By.className("mls"));
					showComment.click();
					//Thread.sleep(2000);
					Thread.sleep((long)span);					
				} catch (Exception e) {

				}
				WebElement addComment = list.get(i).findElement(
						By.className("UFIPagerLink"));
				while ((addComment != null)&&(commentClickNum < 10)) {
					addComment.click();
					//Thread.sleep(2000);
					Thread.sleep((long)span);
					addComment = list.get(i).findElement(
							By.className("UFIPagerLink"));
					commentClickNum++;
				}
			} catch (Exception ex) {
			}

			/**
			 * ���
			 */
			// output(list.get(i), i);

			// --------------------------------------
			try {
				File file = new File("log.txt");
				FileOutputStream fout = new FileOutputStream(file);
				DataOutputStream out = new DataOutputStream(fout);
				out.writeBytes(uid);
				// out.writeBytes(",");
				// out.writeBytes();
				out.writeBytes("	");
				out.close();
				fout.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("ex " + ex);
			}
			// ----------------------------------------
			Schedule.addParserTask(list.get(i).getAttribute("outerHTML"),"timeline");
		}
	}

	/**
	 * 
	 * @param webDriver
	 * @param personID
	 *            ����ȡ�˵���ַ
	 * @param MAXTIMELINE
	 *            �����ȡ��
	 */
	public void getFriends(String uid) {
		int friendsCount = 0;
		// config
		String friends = "https://www.facebook.com/" + uid + "/friends";
		String setscroll = "scroll(0,200000)";
		String friendClassName = "_698";
		double span;

		webDriver.get(friends);

		JavascriptExecutor jse = (JavascriptExecutor) webDriver;
		List<WebElement> list = null;
		try {
			list = webDriver.findElements(By.className(friendClassName));
			friendsCount = list.size();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("ex " + ex);
		}

		try {
			while (true) {
				span = Math.random()*8000 + 500;
				
				jse.executeScript(setscroll);
				try {
					//Thread.sleep(3000);
					Thread.sleep((long)span);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				list = webDriver.findElements(By.className(friendClassName));
				int count = list.size();
				if (count == friendsCount)
					break;
				else
					friendsCount = count;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex);
		}
		/**
		 * ���
		 */
		String friendsitem = "<uid>"+uid+"</uid>"; 
		for (int i = 0; i < friendsCount; i++) {
			friendsitem += list.get(i).getAttribute("outerHTML");
		}
		Schedule.addParserTask(friendsitem,"friend");
	}

	public void parse(String input) {

	}

	/**
	 * ������֣���ÿ����ȡ����TimelineԪ�ش洢Ϊһ��html�ļ�
	 * 
	 * @param webElement
	 *            Ҫ�����Ԫ��
	 * @param index
	 */
	private void output(WebElement webElement, int index) {
		try {
			File file = new File(index + ".html");
			FileOutputStream fout = new FileOutputStream(file);
			DataOutputStream out = new DataOutputStream(fout);
			out.write(webElement.getAttribute("outerHTML").getBytes("UTF-8"));
			out.close();
			fout.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("ex " + ex);
		}
	}

	public void finish() {
		webDriver.quit();
		System.out.print("finish");
	}

	/*
	 * public static void main(String args[]) { GetTimeLine test = new
	 * GetTimeLine(); test.getTimeline("xanthe.chang.31"); test.finish(); //
	 * test.doJob(); // System.out.println("finish"); }
	 */
}
