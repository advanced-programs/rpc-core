package info.hb.rpc.example;

import info.hb.rpc.core.client.Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.config.ConfigUtil;
import zx.soft.utils.log.LogbackUtil;

public class TaskClient {

	private static Logger logger = LoggerFactory.getLogger(TaskClient.class);

	private String ip;
	private int port;
	private int threadsNum;

	public TaskClient() {
		Properties props = ConfigUtil.getProps("rpc.properties");
		ip = props.getProperty("server.ip");
		port = Integer.parseInt(props.getProperty("server.port"));
		threadsNum = Integer.parseInt(props.getProperty("client.threads.num"));
	}

	public static void main(String[] args) {
		TaskClient start = new TaskClient();
		start.run();
	}

	public void run() {
		logger.info("Start client ...");

		try {
			// 连接服务端，设置超时时间为10s
			Client client = new Client(ip, port, 10);
			Task task = client.createProxy(Task.class);

			// 申请线程池
			ThreadPoolExecutor pool = getThreadPoolExector(threadsNum);

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					pool.shutdown();
				}

			}));

			// 需要传输的数据
			String data = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread()
					.getContextClassLoader().getResourceAsStream("data")));) {
				String str = null;
				while ((str = br.readLine()) != null) {
					data += str;
				}
			}

			final AtomicLong COUNT = new AtomicLong(0L);
			// 执行多线程
			while (true) {
				COUNT.addAndGet(1L);
				if (COUNT.get() % 1_0000 == 0) {
					logger.info("Count: {}", COUNT.get());
				}
				pool.execute(new TaskRunnable(task, data));
			}

			// 关闭线程池
			//			pool.shutdown();
			//			pool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			logger.error("Exception: {}", LogbackUtil.expection2Str(e));
			logger.info("Transport data finish!");
		}

	}

	private static class TaskRunnable implements Runnable {

		private Task task;
		private String data;

		public TaskRunnable(Task task, String data) {
			this.task = task;
			this.data = data;
		}

		@Override
		public void run() {
			task.consume(data);
		}

	}

	private static ThreadPoolExecutor getThreadPoolExector(int threadsNum) {

		final ThreadPoolExecutor result = new ThreadPoolExecutor(threadsNum, threadsNum, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(threadsNum * 2), new ThreadPoolExecutor.CallerRunsPolicy());
		result.setThreadFactory(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						logger.error("Thread {} has Exception:{}", t.getName(),
								LogbackUtil.expection2Str(new Exception(e)));
						result.shutdown();
					}

				});
				return t;
			}
		});

		return result;
	}

}
