package info.hb.rpc.core.client;

import info.hb.rpc.core.MarshallingFactory;
import info.hb.rpc.core.domain.Request;
import info.hb.rpc.core.domain.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 远程调用服务. 超时时间默认为5s,如果在15s内没有通信，那么关闭连接
 *
 * @author wanggang
 *
 */
public class ProxyHandler extends ChannelHandlerAdapter implements InvocationHandler {

	private static Logger logger = LoggerFactory.getLogger(ProxyHandler.class);

	private static final AtomicInteger SERIAL = new AtomicInteger(0);

	/**
	 * IO线程接收到的响应
	 */
	private static final BlockingQueue<Response> RESPONSES = new ArrayBlockingQueue<>(1024);

	/**
	 * 发送成功的请求
	 */
	private static final ConcurrentHashMap<Integer, Request> REQUESTS = new ConcurrentHashMap<>();

	private Channel channel = null;

	private String host = null;

	private int port;

	/**
	 * 超时时间，默认5s
	 */
	private int timeout = 5;

	public ProxyHandler(String host, int port, int timeout) {
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.channel = ctx.channel();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 保证channel是连通的
		this.connect();
		// 发送调用请求
		Class<?>[] interfaces = proxy.getClass().getInterfaces();
		final Request request = new Request();
		request.setSerialId(SERIAL.incrementAndGet());
		request.setIntface(interfaces[0].getCanonicalName());
		request.setMethod(method.getName());
		request.setParamtypes(method.getParameterTypes());
		request.setParams(args);
		// 把request写到对端
		channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				REQUESTS.put(request.getSerialId(), request);
			}
		});
		// 等待
		long start = System.currentTimeMillis();
		long end = start;
		synchronized (request) {
			// 超时非常重要，防止通信异常导致的客户端一直等待
			while (request.getSerialId() != -1 && (end - start) < timeout * 1000) {
				request.wait(timeout * 1000 - (end - start));
				end = System.currentTimeMillis();
			}
		}
		if (request.getSerialId() != -1) {
			// 如果发生超时，那么删除请求
			REQUESTS.remove(request.getSerialId());
			logger.error("在 {}s 内未收到服务器的响应.", this.timeout);
			return null;
			//			throw new TimeoutException("在 " + this.timeout + "s 内未收到服务器的响应.");
		} else if (request.getRsp() instanceof Exception) {
			throw (Exception) request.getRsp();
		}
		return request.getRsp();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object in) throws Exception {
		RESPONSES.put((Response) in);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("io线程抛出一个错误: {}", cause);
		this.closeChannel();
	}

	/**
	 * 关闭连接并且清空请求响应队列
	 */
	private synchronized void closeChannel() {
		PROCESS.interrupt();
		this.channel.close().syncUninterruptibly();
		RESPONSES.clear();
		REQUESTS.clear();
		this.channel = null;

	}

	/**
	 * 同服务器建立连接
	 */
	private synchronized void connect() {
		if (channel != null)
			return;
		final EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(MarshallingFactory.createEncoder(), MarshallingFactory.createDecoder(),
							new ReadTimeoutHandler(15), ProxyHandler.this);
				}
			});
			// Start the client.
			ChannelFuture f = b.connect(host, port).sync();
			Channel channel = f.channel();
			channel.closeFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					workerGroup.shutdownGracefully();
				}
			});
			this.channel = channel;
			PROCESS.start();
		} catch (Exception e) {
			workerGroup.shutdownGracefully();
			throw new RuntimeException("连接服务器失败.", e);
		}
	}

	private static Thread PROCESS = new Thread(new Runnable() {

		@Override
		public void run() {
			for (;;) {
				try {
					Response rsp = RESPONSES.take();
					Request req = REQUESTS.remove(rsp.getSerialId());
					if (req != null) {
						synchronized (req) {
							// 设置响应
							req.setRsp(rsp.getEntity());
							// 把serialId设置为-1，标志服务器成功响应
							req.setSerialId(-1);
							req.notify();
						}
					}
				} catch (InterruptedException e) {
					// 退出
					return;
				} catch (Exception e) {
					// 业务线程异常不需要关闭channel
					logger.error("业务线程抛出一个错误:", e);
				}
			}
		}

	});

}
