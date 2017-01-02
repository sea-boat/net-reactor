package com.seaboat.net.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaboat.net.reactor.handler.Handler;

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>Reactor reacts all sockets.</p>
 */
public final class Reactor extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(Reactor.class);
	private final String name;
	private final Selector selector;
	private final ConcurrentLinkedQueue<FrontendConnection> queue;
	private long doCount;
	private Handler handler;
	private BufferPool bufferPool;

	public Reactor(String name, Handler handler) throws IOException {
		this.name = name;
		this.selector = Selector.open();
		this.queue = new ConcurrentLinkedQueue<FrontendConnection>();
		this.handler = handler;
		this.bufferPool = new BufferPool(1024 * 2014 * 512, 10 * 1024);
	}

	final void postRegister(FrontendConnection frontendConnection) {
		queue.offer(frontendConnection);
		this.selector.wakeup();
	}

	@Override
	public void run() {
		final Selector selector = this.selector;
		Set<SelectionKey> keys = null;
		for (;;) {
			++doCount;
			try {
				selector.select(500L);
				register(selector);
				keys = selector.selectedKeys();
				for (SelectionKey key : keys) {
					FrontendConnection connection = null;
					Object attach = key.attachment();
					if (attach != null && key.isValid()) {
						connection = (FrontendConnection) attach;
						if (key.isReadable()) {
							try {
								connection.read();
								handler.handle(connection);
							} catch (IOException e) {
								connection.close();
								LOGGER.warn("IOException happens : ", e);
								continue;
							} catch (Throwable e) {
								LOGGER.warn("Throwable happens : ", e);
								continue;
							}
						}
						if (key.isValid() && key.isWritable()) {
							connection.write();
						}
					} else {
						key.cancel();
					}
				}
			} catch (Throwable e) {
				LOGGER.warn("exception happens selecting : ", e);
			} finally {
				if (keys != null) {
					keys.clear();
				}
			}
		}
	}

	private void register(Selector selector) {
		FrontendConnection c = null;
		if (queue.isEmpty()) {
			return;
		}
		while ((c = queue.poll()) != null) {
			try {
				c.register(selector);
			} catch (Throwable e) {
				LOGGER.warn("ClosedChannelException happens : ", e);
			}
		}
	}

	final Queue<FrontendConnection> getRegisterQueue() {
		return queue;
	}

	final long getReactCount() {
		return doCount;
	}

	public BufferPool getBufferPool() {
		return bufferPool;
	}

}
