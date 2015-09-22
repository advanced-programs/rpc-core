package info.hb.rpc.example;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.log.LogbackUtil;

public class TaskImpl implements Task {

	private static Logger logger = LoggerFactory.getLogger(TaskImpl.class);

	// 请求总次数
	private static final AtomicLong REQUEST_COUNT = new AtomicLong(0L);
	// 请求总字节大小
	private static final AtomicLong REQUEST_BYTE = new AtomicLong(0L);

	// 起始时间，秒
	private static long startTime = System.currentTimeMillis() / 1000L;

	static {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1_000);
						long timeSpan = System.currentTimeMillis() / 1000L - startTime;
						if (timeSpan == 0L) {
							timeSpan = 1L;
						}
						// 次/s
						long qps = REQUEST_COUNT.get() / timeSpan;
						// MB/s
						long tps = REQUEST_BYTE.get() / (timeSpan * 1_000_000);
						logger.info("QPS: {}  ,  TPS: {}.", qps, tps);
					} catch (InterruptedException e) {
						logger.error("Exception: {}", LogbackUtil.expection2Str(e));
					}
				}
			}

		});

		thread.start();
	}

	@Override
	public void consume(String data) {
		try {
			// 统计次数和字节数
			REQUEST_COUNT.addAndGet(1L);
			REQUEST_BYTE.addAndGet(data.getBytes("UTF-8").length + 0L);
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception: {}", LogbackUtil.expection2Str(e));
			//			throw new RuntimeException(e);
		}
	}

}
