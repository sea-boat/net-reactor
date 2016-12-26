package com.seaboat.net.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>The reactor bootstrap.</p>
 */
public class Bootstrap {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Bootstrap.class);
	private static String acceptorName = "acceptor-thread";
	private static String host = "localhost";
	private static int port = 6789;

	public static void main(String[] args) {
		try {
			LOGGER.info("starting up ......");
			ReactorPool reactorPool = new ReactorPool(Runtime.getRuntime().availableProcessors());
			new Acceptor(reactorPool, acceptorName, host, port).start();
			LOGGER.info("started up successfully.");
			while (true) {
				Thread.sleep(300 * 1000);
			}
		} catch (Throwable e) {
			LOGGER.error(" launch error", e);
			System.exit(-1);
		}
	}
}
