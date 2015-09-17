package info.hb.rpc.core;

public class RemoteException extends RuntimeException {

	private static final long serialVersionUID = 338586689117231408L;

	public String message;

	public RemoteException() {
	}

	public RemoteException(String message) {
		this.message = message;
	}

}
