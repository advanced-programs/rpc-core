package info.hb.rpc.core.domain;

public class QpsAndTps {

	// 每秒请求次数
	private double qps;
	// 每秒请求字节数
	private double tps;

	public QpsAndTps() {
		super();
	}

	public QpsAndTps(double qps, double tps) {
		super();
		this.qps = qps;
		this.tps = tps;
	}

	public double getQps() {
		return qps;
	}

	public void setQps(double qps) {
		this.qps = qps;
	}

	public double getTps() {
		return tps;
	}

	public void setTps(double tps) {
		this.tps = tps;
	}

}
