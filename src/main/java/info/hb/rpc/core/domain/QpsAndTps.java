package info.hb.rpc.core.domain;

public class QpsAndTps {

	// 每秒请求次数
	private long qps;
	// 每秒请求字节数
	private long tps;

	public QpsAndTps() {
		super();
	}

	public QpsAndTps(long qps, long tps) {
		super();
		this.qps = qps;
		this.tps = tps;
	}

	public long getQps() {
		return qps;
	}

	public void setQps(long qps) {
		this.qps = qps;
	}

	public long getTps() {
		return tps;
	}

	public void setTps(long tps) {
		this.tps = tps;
	}

}
