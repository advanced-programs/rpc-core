package info.hb.rpc.core.server;

import info.hb.rpc.core.RemoteException;
import info.hb.rpc.core.domain.Request;
import info.hb.rpc.core.domain.Response;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.config.ConfigUtil;

public class BusinessHandler extends ChannelHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(BusinessHandler.class);

	private static final BlockingQueue<Request> REQUESTS = new ArrayBlockingQueue<>(1024);

	private static final Properties PROPS = ConfigUtil.getProps("conf.properties");

	private static final int THREADS_NUM = 32;

	static {
		// 使用线程池执行
		ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUM);
		Runnable r = new Runnable() {

			@Override
			public void run() {
				for (;;) {
					Request req = null;
					Object ret = null;
					try {
						req = REQUESTS.take();
					} catch (InterruptedException e) {
						return;
					}
					try {
						// 查找方法，并且执行
						Class<?> clazz = Class.forName(PROPS.getProperty(req.getIntface()));
						Object impl = clazz.newInstance();
						Method method = clazz.getDeclaredMethod(req.getMethod(), req.getParamtypes());
						ret = method.invoke(impl, req.getParams());
					} catch (InvocationTargetException e) {
						ret = e.getCause();
					} catch (Exception e) {
						logger.error("业务线程抛出一个错误", e);
						ret = new RemoteException(e.getMessage());
					}
					// 根据返回类型构造响应
					req.getChannel().writeAndFlush(new Response(req.getSerialId(), ret));
				}
			}

		};
		for (int i = 0; i < THREADS_NUM; i++) {
			executor.execute(r);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object in) throws Exception {
		Request req = (Request) in;
		req.setChannel(ctx.channel());
		REQUESTS.put(req);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		cause.printStackTrace();
	}

}