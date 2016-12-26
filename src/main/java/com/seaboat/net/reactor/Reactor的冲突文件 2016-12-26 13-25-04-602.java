package com.seaboat.net.reactor;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Channel;
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
 * <p>This Acceptor provide a NIO mode to accept client sockets.</p>
 */
public final class Reactor extends Thread{
	private static final Logger LOGGER = LoggerFactory.getLogger(Reactor.class);
	private final String name;
	private final Selector selector;
	private final ConcurrentLinkedQueue<Channel> queue;
	private long doCount;


	public Reactor(String name) throws IOException {
		this.name = name;
		this.selector = Selector.open();
		this.queue = new ConcurrentLinkedQueue<Channel>();
	}

	final void postRegister(Channel c) {
		queue.offer(c);
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
					AbstractConnection con = null;
					try {
						Object att = key.attachment();
						if (att != null && key.isValid()) {
							con = (AbstractConnection) att;
							if (key.isReadable()) {
								try {
									con.asynRead();
								} catch (IOException ioe) {
									con.close("program err:"
											+ ioe.getMessage());
									continue;
								} catch (Throwable e) {
									LOGGER.warn("caught err:", e);
									continue;
								}
							}
							if (key.isValid() && key.isWritable()) {
								con.doNextWriteCheck();
							}
						} else {
							key.cancel();
						}
					} catch (Throwable e) {
						if (e instanceof CancelledKeyException) {
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug(con + " socket key canceled");
							}
						} else {

							LOGGER.warn(con + " ", e);
						}
					}
				}
			} catch (Throwable e) {
				LOGGER.warn(name, e);
			} finally {
				if (keys != null) {
					keys.clear();
				}
			}
		}
	}

	private void register(Selector selector) {
		Channel c = null;
		if (registerQueue.isEmpty()) {
			return;
		}

		while ((c = queue.poll()) != null) {
			try {
				((NIOSocketWR) c.getSocketWR()).register(selector);
				c.register();
			} catch (Throwable e) {
				LOGGER.warn("register error ", e);
				c.close("register err");
			}
		}
	}

	final Queue<Channel> getRegisterQueue() {
		return queue;
	}

	final long getReactCount() {
		return doCount;
	}

}
