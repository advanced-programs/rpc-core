package info.hb.rpc.core.domain;

import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = 5163712330318164383L;

	private int serialId;

	private Object entity;

	public Response() {
		super();
	}

	public Response(int serialId, Object entity) {
		super();
		this.serialId = serialId;
		this.entity = entity;
	}

	public int getSerialId() {
		return serialId;
	}

	public void setSerialId(int serialId) {
		this.serialId = serialId;
	}

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

}
