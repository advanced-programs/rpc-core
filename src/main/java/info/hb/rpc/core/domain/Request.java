package info.hb.rpc.core.domain;

import io.netty.channel.Channel;

import java.io.Serializable;

public class Request implements Serializable {

	private static final long serialVersionUID = 3011057235311298484L;

	private int serialId;

	/**
	 * 接口名称
	 */
	private String intface;

	/**
	 * 方法名
	 */
	private String method;

	/**
	 * 参数类型
	 */
	private Class<?>[] paramtypes;

	/**
	 * 参数
	 */
	private Object[] params;

	/**
	 * 连接
	 */
	private transient Channel channel;

	/**
	 * 响应
	 */
	private transient Object rsp;

	public int getSerialId() {
		return serialId;
	}

	public void setSerialId(int serialId) {
		this.serialId = serialId;
	}

	public String getIntface() {
		return intface;
	}

	public void setIntface(String intface) {
		this.intface = intface;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Class<?>[] getParamtypes() {
		return paramtypes;
	}

	public void setParamtypes(Class<?>[] paramtypes) {
		this.paramtypes = paramtypes;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public Object getRsp() {
		return rsp;
	}

	public void setRsp(Object rsp) {
		this.rsp = rsp;
	}

}
