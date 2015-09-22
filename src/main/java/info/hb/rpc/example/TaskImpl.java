package info.hb.rpc.example;

import info.hb.rpc.core.domain.QpsAndTps;
import info.hb.rpc.core.redis.RedisClient;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.json.JsonUtils;
import zx.soft.utils.log.LogbackUtil;

public class TaskImpl implements Task {

	private static Logger logger = LoggerFactory.getLogger(TaskImpl.class);

	// 请求总次数
	private static final AtomicLong REQUEST_COUNT = new AtomicLong(0L);
	// 请求总字节大小
	private static final AtomicLong REQUEST_BYTE = new AtomicLong(0L);

	// 起始时间，秒
	private static long startTime = System.currentTimeMillis() / 1000L;

	// 存放1秒之内的请求次数和请求字节大小，实时更新
	private static final List<QpsAndTps> QPS_TPS = new ArrayList<>();

	private static final RedisClient REDIS_CLIENT = new RedisClient();
	private static final String KEY = "cache-qps-tps";

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
						long qps = REQUEST_COUNT.get() / timeSpan;
						long tps = REQUEST_BYTE.get() / (timeSpan * 1_000_000);
						// 保存数据
						QPS_TPS.clear(); // 清空数据
						QPS_TPS.add(new QpsAndTps(qps, tps));
						// 写入Redis中
						REDIS_CLIENT.deleteKey(KEY); // 清空数据
						REDIS_CLIENT.addRecord(KEY, JsonUtils.toJsonWithoutPretty(QPS_TPS));
						logger.info("QPS: {}  ,  TPS: {}.", QPS_TPS.get(0).getQps(), QPS_TPS.get(0).getTps());
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
