package com.seaboat.net.reactor.handler;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaboat.net.reactor.FrontendConnection;

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>Demo.</p>
 */
public class MyHandler implements Handler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MyHandler.class);
	private long readSize;

	/**
	 * The logic to deal with the received data.
	 *  
	 * It means that reactor will trigger this function once the data is received.
	 * @throws IOException 
	 */
	public void handle(FrontendConnection connection) throws IOException {
		Buffer buff = connection.getReadBuffer();
		readSize = +readSize + buff.position();
		LOGGER.info(connection.getId() + " connection has receive " + readSize);
		if (readSize % 5 == 0) {
			ByteBuffer sendBuffer = ByteBuffer.allocate(10);
			;
			sendBuffer.wrap("hello".getBytes());
			connection.getWriteQueue().add(sendBuffer);
			connection.write();
		}

	}

}
