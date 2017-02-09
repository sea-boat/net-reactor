package com.seaboat.net.reactor.connection;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaboat.net.reactor.Reactor;

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>This is a abstraction of client connection.</p>
 */
public class Connection {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Connection.class);
	private long id;
	private SocketChannel channel;
	private SelectionKey selectionKey;
	private ByteBuffer readBuffer;
	private ConcurrentLinkedQueue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	private Reactor reactor;
	private long lastReadTime;
	private long lastWriteTime;
	private long createTime;
	private long netInBytes;
	private long netOutBytes;
	private volatile boolean isClose = false;
	private List<ConnectionEventHandler> eventHandlers = new LinkedList<ConnectionEventHandler>();

	public Connection(SocketChannel channel, long id, Reactor reactor) {
		this.id = id;
		this.channel = channel;
		this.reactor = reactor;
		this.createTime = System.currentTimeMillis();
		this.lastReadTime = createTime;
		this.lastWriteTime = createTime;
		this.readBuffer = reactor.getReactorPool().getBufferPool().allocate();
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public long getId() {
		return id;
	}

	public void read() throws IOException {
		this.lastReadTime = System.currentTimeMillis();
		int size = channel.read(readBuffer);
		if (size == -1) {
			// client closes the connection
			this.close();
			LOGGER.warn(" it return -1 when doing a read from channel,client closes the connection,we close the connection also.");
		} else if (size == 0) {
			if (!channel.isOpen()) {
				this.close();
				LOGGER.warn(" it return 0 when doing a read from channel,and channel is not open,we close the connection.");
				throw new IOException();
			}
		}
		if (size > 0)
			netInBytes += size;
	}

	public void close() throws IOException {
		this.processEvent(ConnectionEvents.CLOSE);
		channel.close();
		isClose = true;
		if (readBuffer != null) {
			reactor.getReactorPool().getBufferPool().recycle(readBuffer);
			this.readBuffer = null;
		}
	}

	public void write() throws IOException {
		this.lastWriteTime = System.currentTimeMillis();
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
				netOutBytes += len;
			}
			reactor.getReactorPool().getBufferPool().recycle(buffer);
		}
		selectionKey.interestOps(selectionKey.interestOps()
				& ~SelectionKey.OP_WRITE);
	}

	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}

	public void WriteToQueue(ByteBuffer buffer) {
		writeQueue.add(buffer);
	}

	public void register(Selector selector) throws Throwable {
		selectionKey = channel.register(selector, SelectionKey.OP_READ, this);
		processEvent(ConnectionEvents.REGISTE);
	}

	public void processEvent(int event) {
		for (ConnectionEventHandler handler : eventHandlers) {
			if (handler.getEventType() == event)
				handler.event(this);
		}
	}

	public void addEventHandler(ConnectionEventHandler handler) {
		eventHandlers.add(handler);
	}

	public long getLastReadTime() {
		return lastReadTime;
	}

	public long getLastWriteTime() {
		return lastWriteTime;
	}

	public long getNetInBytes() {
		return netInBytes;
	}

	public long getNetOutBytes() {
		return netOutBytes;
	}

	public Reactor getReactor() {
		return reactor;
	}

	public boolean isClose() {
		return isClose;
	}

	public void setClose(boolean isClose) {
		this.isClose = isClose;
	}

}
