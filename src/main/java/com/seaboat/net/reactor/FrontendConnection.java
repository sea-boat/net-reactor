package com.seaboat.net.reactor;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
	private SocketChannel channel;
	private SelectionKey selectionKey;
	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;
	private static int BYFFERSIZE = 1024;

	public FrontendConnection(SocketChannel channel, long id) {
		this.id = id;
		this.channel = channel;
		this.readBuffer = ByteBuffer.allocate(BYFFERSIZE);
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public long getId() {
		return id;
	}

	public void read() throws IOException {
		channel.read(readBuffer);
	}

	public void close() throws IOException {
		channel.close();
	}

	public void write() throws IOException {
		while (writeBuffer.hasRemaining()) {
			int len = channel.write(writeBuffer);
			if (len < 0) {
				throw new EOFException();
			}
			if (len == 0) {
				selectionKey.interestOps(selectionKey.interestOps()
						| SelectionKey.OP_WRITE);
				selectionKey.selector().wakeup();
				break;
			}
		}
	}

	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}

	public ByteBuffer getWriteBuffer() {
		return writeBuffer;
	}

	public void register(Selector selector) throws Throwable {
		selectionKey = channel.register(selector, SelectionKey.OP_READ, this);
	}

}
