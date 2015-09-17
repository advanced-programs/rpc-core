package info.hb.rpc.core.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class Client {

	private InvocationHandler handler = null;

	@SuppressWarnings("unchecked")
	public <T> T createProxy(Class<T> clazz) {
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
	}

	public Client(String host, int port) {
		this(host, port, 5);
	}

	/**
	 * 创建客户端。如果成功，说明同服务器建立了连接。
	 *
	 * @param host
	 * @param port
	 * @param timeout
	 */
	public Client(String host, int port, int timeout) {
		this.handler = new ProxyHandler(host, port, timeout);
	}

}
