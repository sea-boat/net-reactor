package com.seaboat.net.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>This is a abstraction of frontend.</p>
 */
public class FrontendConnection {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FrontendConnection.class);
	private long id;
	private long readSize;
	private SocketChannel channel;
	private ByteBuffer buffer;

	public FrontendConnection(SocketChannel channel, long id) {
		this.id = id;
		this.channel = channel;
		this.buffer = ByteBuffer.allocate(256);
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public long getId() {
		return id;
	}

	public void read() throws IOException {
		channel.read(buffer);
		if (buffer.position() >= 256) {
			readSize = +readSize + 256;
			LOGGER.info(this.id + " connection has receive " + readSize);
			buffer.clear();
		}
	}

	public void close(String string) {
		try {
			channel.close();
		} catch (IOException e) {
			LOGGER.warn("IOException happens when closing a channel : ", e);
		}
	}

	public void write(ByteBuffer sendBuffer) {
		try {
			channel.write(sendBuffer);
		} catch (IOException e) {
			LOGGER.warn("IOException happens when writing a channel : ", e);
		}
	}

}
