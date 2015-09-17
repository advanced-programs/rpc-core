package info.hb.rpc.core.client;

public class TimeoutException extends RuntimeException {

	private static final long serialVersionUID = 668044398341911016L;

	public TimeoutException(String message) {
		super(message);
	}

}
