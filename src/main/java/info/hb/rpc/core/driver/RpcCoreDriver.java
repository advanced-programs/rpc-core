package info.hb.rpc.core.driver;

import info.hb.rpc.example.TaskClient;
import info.hb.rpc.example.TaskServer;
import zx.soft.utils.driver.ProgramDriver;

/**
 * 驱动类
 *
 * @author wanggang
 *
 */
public class RpcCoreDriver {

	/**
	 * 主函数
	 */
	public static void main(String[] args) {

		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();
		try {
			pgd.addClass("taskServer", TaskServer.class, "启动Server");
			pgd.addClass("taskClient", TaskClient.class, "启动Client");
			pgd.driver(args);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		System.exit(exitCode);

	}

}
