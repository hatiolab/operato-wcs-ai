package xyz.elidom.util;

/**
 * 스레드 관련 유틸리티
 * 
 * @author shortstop
 */
public class ThreadUtil {
	/**
	 * 동기 방식의 스레드 실행.
	 * 
	 * @param runnable
	 */
	public static String doSynch(Runnable runnable) {
		return new SimpleThread(runnable).start();
	}

	/**
	 * 비동기 방식의 스레드 실행.
	 * 
	 * @param runnable
	 */
	public static void doAsynch(Runnable runnable) {
		new AsynchThread(runnable).start();
	}

	/**
	 * sleepTime 동안 Thread Sleep
	 * 
	 * @param sleepTime
	 */
	public static void sleep(int sleepTime) {
		sleep(ValueUtil.toLong(sleepTime));
	}

	/**
	 * sleepTime 동안 Thread Sleep
	 * 
	 * @param sleepTime
	 */
	public static void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

/**
 * 동기 방식의 스레드 클래스.
 * 
 * @author shortstop
 */
class SimpleThread {
	Runnable runnable;

	public SimpleThread(Runnable runnable) {
		this.runnable = runnable;
	}

	public String start() {
		double start = System.currentTimeMillis();
		runnable.run();
		return ValueUtil.toString(System.currentTimeMillis() - start);
	}
}

/**
 * 비동기 방식의 스레드 클레스.
 * 
 * @author shortstop
 */
class AsynchThread implements Runnable {
	Runnable runnable;

	public AsynchThread(Runnable runnable) {
		this.runnable = runnable;
	}

	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		runnable.run();
	}
}