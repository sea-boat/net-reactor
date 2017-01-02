package com.seaboat.net.reactor;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	private static int BYFFERSIZE = 1024;
	protected ConcurrentLinkedQueue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	private Reactor reactor;

	public FrontendConnection(SocketChannel channel, long id, Reactor reactor) {
		this.id = id;
		this.channel = channel;
		this.reactor = reactor;
		//allocate byteBuffer until channel closed.
		this.readBuffer = reactor.getBufferPool().allocate();
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
		if (readBuffer != null) {
			reactor.getBufferPool().recycle(readBuffer);
			this.readBuffer = null;
		}
	}

	public void write() throws IOException {
		ByteBuffer buffer;
		while ((buffer = writeQueue.poll()) != null) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				int len = channel.write(buffer);
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
		selectionKey.interestOps(selectionKey.interestOps()
				& ~SelectionKey.OP_WRITE);
	}

	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}

	public ConcurrentLinkedQueue<ByteBuffer> getWriteQueue() {
		return writeQueue;
	}

	public void register(Selector selector) throws Throwable {
		selectionKey = channel.register(selector, SelectionKey.OP_READ, this);
	}

}
