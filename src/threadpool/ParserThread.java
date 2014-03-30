package threadpool;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.mongodb.*;

import spider.PaserDate;

public class ParserThread extends Thread {

	public static final int PARSERTHREAD_IDLE = 0;
	public static final int PARSERTHREAD_BLOCKED = 1;
	public static final int PARSERTHREAD_BUSY = 2;

	public static int mid = 0;

	/** the current state of this thread - idle, blocked, or busy. */
	protected int state;

	/** Whether this instance is assigned a task. */
	protected boolean assigned;

	/** Whether we should keep alive this thread. */
	protected boolean running;

	/** Threadpool this worker is part of. */
	protected ParserThreadPool stp;

	/** Task this worker is assigned to. */
	protected ParserTask task;

	private PaserDate pd = new PaserDate();

	/**
	 * Public constructor.
	 * 
	 * @param stp
	 *            thread pool this worker is part of
	 * @param name
	 *            name of the thread
	 * @param i
	 *            index in the pool
	 */
	public ParserThread(ParserThreadPool stp, String name, int i) {
		super(stp, name + " " + i);
		this.stp = stp;
		running = false;
		assigned = false;
		state = PARSERTHREAD_IDLE;
	}

	/**
	 * Tests whether this worker thread instance can be assigned a task.
	 * 
	 * @return whether we're capable of handling a task.
	 */
	public boolean isAvailable() {
		return (!assigned) && running;
	}

	/**
	 * Determines whether we're occupied.
	 * 
	 * @return boolean value representing our occupation
	 */
	public boolean isOccupied() {
		return assigned;
	}

	/**
	 * Method that allows the threadPool to assign the worker a Task.
	 * 
	 * @param task
	 *            ParserTask to be executed.
	 */
	public synchronized void assign(ParserTask task) {
		if (!running) {
			// SHOULDN'T HAPPEN WITHOUT BUGS
			throw new RuntimeException(
					"THREAD NOT RUNNING, CANNOT ASSIGN TASK !!!");
		}
		if (assigned) {
			// SHOULDN'T HAPPEN WITHOUT BUGS
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
		if (!running) {
			throw new RuntimeException("THREAD NOT RUNNING - CANNOT STOP !");
		}
		if (assigned) {
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
	 * 
	 * @return
	 */
	public int getstate() {
		return state;
	}

	public synchronized List<DBObject> parse(String WebPage, String type) {
		// config
		// String createrInfoClassName = "_1_s";
		String createTimeClassName = "_1_n";
		String contentClassName = "userContent";
		String photoClassName = "_6i9";
		String photoWrapClassName = "photoWrap";
		String commentContainerClassName = "UFIComment";
		String commentActorClassName = "UFICommentActorName";
		String commentStampClassName = "livetimestamp";
		String userClassName = "fwb";
		// pattern
		//Pattern uidPattern = Pattern.compile("id=\\d*");
		Pattern uidPattern = Pattern.compile("www.facebook.com/\\S*\\\"");
		Pattern unamePattern = Pattern.compile("www.facebook.com/\\S*\\?");
		String result = "";
		
		List<DBObject> list =  new ArrayList<DBObject>();
		
		Document doc = Jsoup.parse(WebPage);

		if (type.compareTo("timeline") == 0) {
			DBObject timeline = new BasicDBObject(); 
			int fid = ++mid;
			// UserName
			String uid = "";
			try {
				Elements usersList = doc.getElementsByClass(userClassName);
				for (Element userInfo : usersList) {
					Element user = userInfo.getElementsByTag("a").first();
					String userName = user.text();
					// result += "\n" +"name:" + userName;
					uid = user.outerHtml();
					Matcher matcher = uidPattern.matcher(uid);
					while (matcher.find()) {
						//uid = matcher.group().substring(3);
						uid = matcher.group().substring(17);
						uid = uid.substring(0, uid.length() - 1);
						if (uid.indexOf('?') != -1)
							uid = uid.substring(0,uid.indexOf('?'));
						break;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			result += mid + ",," + uid + ",";
			timeline.put("mid", mid);
			timeline.put("uid", uid);

			String mdate = "";
			try {
				Elements createTimes = doc
						.getElementsByClass(createTimeClassName);
				for (Element createTime : createTimes) {
					Elements times = createTime.getElementsByTag("abbr");
					String time = null;
					for (Element element : times) {
						time = element.attr("title");
					}
					Date date = pd.getDate(time);
					mdate = date.toString();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			result += mdate + ",";
			timeline.put("date", mdate);

			int photoNum = 0;
			String url = "";
			try {

				Elements photos = doc.getElementsByClass(photoClassName);
				photoNum = photos.size();
				Map<String, Object> photosMap = new HashMap<String, Object>();
				Integer i = 0;
				// String url = "";
				for (Element photo : photos) {
					String imgUrl = photo.attr("href");
					Elements photoWrap = photo
							.getElementsByClass(photoWrapClassName);
					String imgSrc = photoWrap.first().getElementsByTag("img")
							.first().attr("src");
					url += imgUrl + ";";
					photosMap.put(i.toString(), imgSrc);
					i++;
					// result += "\n" +imgSrc;
				}
				if (photoNum != 0)
					timeline.put("photos", photosMap);
			} catch (Exception e) {
				// TODO: handle exception
			}
			result += photoNum + ",";
			result += url + ",";
			

			String content = "";
			try {
				Elements contentEs = doc.getElementsByClass(contentClassName);
				for (Element contentE : contentEs) {
					content += contentE.text();

				}

			} catch (Exception e) {
				// TODO: handle exception
			}
			result += content + "\n";
			timeline.put("content", content);
			list.add(timeline);

			try {
				Elements commentContainers = doc
						.getElementsByClass(commentContainerClassName);
				// if (!commentContainers.isEmpty())
				// result += "\n" +"Comments:";
				int i = 0;

				for (Element commentContainer : commentContainers) {
					DBObject comment = new BasicDBObject();
					mid++;
					result += mid + "," + fid + ",";
					comment.put("mid", mid);
					comment.put("fid", fid);

					Element commentActor = commentContainer.getElementsByClass(
							commentActorClassName).first();
					String actorName = commentActor.text();
					// result += "\n" +"name:" + actorName;
					uid = commentActor.outerHtml();
					Matcher matcher = uidPattern.matcher(uid);
					while (matcher.find()) {
						//uid = matcher.group().substring(3);
						uid = matcher.group().substring(17);
						uid = uid.substring(0, uid.length() - 1);
						if (uid.indexOf('?') != -1)
							uid = uid.substring(0,uid.indexOf('?'));
						result += uid + ",";
						comment.put("uid", uid);
						Schedule.addWorkerTask(uid);
						break;
					}

					Element commentStamp = commentContainer.getElementsByClass(
							commentStampClassName).first();
					Date date = pd.getDate(commentStamp.attr("title"));
					result += date.toString() + ",0,,";
					comment.put("date", date.toString());

					Element commentContent = commentContainer
							.getElementsByClass("UFICommentContent").first();
					Elements contents = commentContent.getElementsByTag("span");
					content = null;
					for (Element c : contents) {
						if (!c.text().isEmpty()) {
							content = c.text();
							break;
						}

					}
					result += content + "\n";
					comment.put("content", content);
					list.add(comment);
					i++;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else {
			DBObject friend = new BasicDBObject();
			result = "";
			// UserName
			String uid = "";
			try{
				Element uidct = doc.getElementsByTag("uid").first();
				uid = uidct.ownText();
				friend.put("uid", uid);
			} catch (Exception e) {
				
			}
			Integer i = 0;
			try {
				Elements usersList = doc.getElementsByClass(userClassName);
				for (Element userInfo : usersList) {
					Element user = userInfo.getElementsByTag("a").first();
					String userName = user.text();
					// result += "\n" +"name:" + userName;
					String fid = user.attr("href");
					Matcher matcher = unamePattern.matcher(fid);
					while (matcher.find()) {
						fid = matcher.group().substring(17);
						fid = fid.substring(0, fid.length() - 1);
						Schedule.addWorkerTask(fid);
						break;
					}
					
					result += uid + "," + fid + "\n";
					friend.put(i.toString(), fid);
					i++;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			list.add(friend);
		}
		return list;
	}

	/**
	 * Thread's overridden run method.
	 */
	public synchronized void run() throws MongoException{
		running = true;
		
		Mongo mg = null;
		try {
			mg = new Mongo();
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		DB db = mg.getDB("test");
		DBCollection timelines = db.getCollection("timelines");
		DBCollection friends = db.getCollection("friends");
		

		// Log log = LogFactory.getLog(WorkerThread.class);
		// log.debug("Worker thread (" + this.getName() + ") born");
		File file = new File("timeline.txt");
		File file_friends = new File("friends.txt");
		FileOutputStream fout = null;
		FileOutputStream fout_friends = null;
		try {
			fout = new FileOutputStream(file, true);
			fout_friends = new FileOutputStream(file_friends, true);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DataOutputStream out = new DataOutputStream(fout);
		DataOutputStream out_friends = new DataOutputStream(fout_friends);
		try {
			out.write("mid,fmid,uid,date,photonum,photo,content\n"
					.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		synchronized (stp) {
			stp.notify();
		}

		while (running) {
			if (assigned) {
				// state = PARSERTHREAD_BLOCKED;
				// task.prepare();
				state = PARSERTHREAD_BUSY;
				try {
					// task.execute();
					// task.tearDown();

					// Parsing code

					String we = task.html;
					String line = "";
					String tp = task.type;

					if (tp.compareTo("timeline") == 0) {
						timelines.insert(parse(we, tp));
					} else {
						friends.insert(parse(we, tp));
					}
					

				} catch (Exception e) {
					// log.fatal("PANIC! Task " + task + " threw an excpetion!",
					// e);
					System.out.println("Parser Thread:"
							+ Thread.currentThread().getName()
							+ " failed and exit...");
					System.out.println(e.getMessage());
					// System.exit(1);
				}

				synchronized (stp) {
					assigned = false;
					task = null;
					state = PARSERTHREAD_IDLE;
					stp.notify();
					this.notify(); // if some thread is blocked in
									// stopRunning();
				}

			}
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
