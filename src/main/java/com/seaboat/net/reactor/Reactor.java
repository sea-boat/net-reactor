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

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>Reactor reacting all sockets.</p>
 */
public final class Reactor extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(Reactor.class);
	private final String name;
	private final Selector selector;
	private final ConcurrentLinkedQueue<FrontendConnection> queue;
	private long doCount;

	public Reactor(String name) throws IOException {
		this.name = name;
		this.selector = Selector.open();
		this.queue = new ConcurrentLinkedQueue<FrontendConnection>();
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
							} catch (IOException ioe) {
								connection.close("IOException happens when read : " + ioe.getMessage());
								continue;
							} catch (Throwable e) {
								LOGGER.warn("Throwable happens : ", e);
								continue;
							}
						}
						if (key.isValid() && key.isWritable()) {
							String str = "hello";
							ByteBuffer sendBuffer = ByteBuffer.wrap(str
									.getBytes("UTF-8"));
							connection.write(sendBuffer);
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
				c.getChannel().register(selector, SelectionKey.OP_READ, c);
			} catch (ClosedChannelException e) {
				LOGGER.warn("exception happens when registering: ", e);
			}
		}
	}

	final Queue<FrontendConnection> getRegisterQueue() {
		return queue;
	}

	final long getReactCount() {
		return doCount;
	}

}
