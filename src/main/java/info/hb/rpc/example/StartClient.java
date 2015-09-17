package info.hb.rpc.example;

import info.hb.rpc.core.client.Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.config.ConfigUtil;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.threads.ApplyThreadPool;

public class StartClient {

	private static Logger logger = LoggerFactory.getLogger(StartClient.class);

	private String ip;
	private int port;
	private int threadsNum;

	public StartClient() {
		Properties props = ConfigUtil.getProps("rpc.properties");
		ip = props.getProperty("server.ip");
		port = Integer.parseInt(props.getProperty("server.port"));
		threadsNum = Integer.parseInt(props.getProperty("client.threads.num"));
	}

	public static void main(String[] args) {
		StartClient start = new StartClient();
		start.run();
	}

	public void run() {
		logger.info("Start client ...");

		try {
			// 连接服务端
			Client client = new Client(ip, port);
			Task task = client.createProxy(Task.class);

			// 申请线程池
			ThreadPoolExecutor pool = ApplyThreadPool.getThreadPoolExector(threadsNum);

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

			//			final AtomicLong COUNT = new AtomicLong(0L);
			// 执行多线程
			while (true) {
				//				logger.info("Count: {}", COUNT.addAndGet(1L));
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

}
