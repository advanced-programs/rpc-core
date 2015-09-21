package info.hb.rpc.example;

import info.hb.rpc.core.server.Server;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.utils.config.ConfigUtil;
import zx.soft.utils.log.LogbackUtil;

public class TaskServer {

	private static Logger logger = LoggerFactory.getLogger(TaskServer.class);

	private int port;

	public TaskServer() {
		Properties props = ConfigUtil.getProps("rpc.properties");
		port = Integer.parseInt(props.getProperty("server.port"));
	}

	public static void main(String[] args) throws Exception {
		TaskServer start = new TaskServer();
		start.run();
	}

	public void run() {
		logger.info("Start server at port {} ....", port);
		try {
			new Server().start(port);
		} catch (Exception e) {
			logger.error("Exception: {}", LogbackUtil.expection2Str(e));
			throw new RuntimeException(e);
		}
	}

}
