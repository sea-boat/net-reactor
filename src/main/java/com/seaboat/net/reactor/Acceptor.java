package com.seaboat.net.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>This Acceptor provides a NIO mode to accept client sockets.</p>
 */
public final class Acceptor extends Thread {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Acceptor.class);
	private final int port;
	private final Selector selector;
	private final ServerSocketChannel serverChannel;
	private long acceptCount;
	private static final AcceptIdGenerator IdGenerator = new AcceptIdGenerator();
	private ReactorPool reactorPool;

	public Acceptor(ReactorPool reactorPool, String name, String bindIp,
			int port) throws IOException {
		super.setName(name);
		this.port = port;
		this.selector = Selector.open();
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);
		this.serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		this.serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
		this.serverChannel.bind(new InetSocketAddress(bindIp, port), 100);
		this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.reactorPool = reactorPool;
	}

	public int getPort() {
		return port;
	}

	public long getAcceptCount() {
		return acceptCount;
	}

	@Override
	public void run() {
		final Selector selector = this.selector;
		for (;;) {
			++acceptCount;
			try {
				selector.select(1000L);
				Set<SelectionKey> keys = selector.selectedKeys();
				try {
					for (SelectionKey key : keys) {
						if (key.isValid() && key.isAcceptable()) {
							accept();
						} else {
							key.cancel();
						}
					}
				} finally {
					keys.clear();
				}
			} catch (Throwable e) {
				LOGGER.warn(getName(), e);
			}
		}
	}

	/**
	 * Accept client sockets.
	 */
	private void accept() {
		SocketChannel channel = null;
		try {
			channel = serverChannel.accept();
			channel.configureBlocking(false);
			channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
			channel.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
			channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
			reactorPool.getNextReactor().postRegister(
					new FrontendConnection(channel, IdGenerator.getId()));
		} catch (Throwable e) {
			closeChannel(channel);
			LOGGER.warn(getName(), e);
		}
	}

	/**
	 * Close a channel.
	 * 
	 * @param channel
	 */
	private static void closeChannel(SocketChannel channel) {
		if (channel == null) {
			return;
		}
		Socket socket = channel.socket();
		if (socket != null) {
			try {
				socket.close();
				LOGGER.info("channel close.");
			} catch (IOException e) {
				LOGGER.warn("IOException happens when closing socket : ", e);
			}
		}
		try {
			channel.close();
		} catch (IOException e) {
			LOGGER.warn("IOException happens when closing channel : ", e);
		}
	}

	/**
	 * ID Generator.
	 */
	private static class AcceptIdGenerator {
		private static final long MAX_VALUE = 0xffffffffL;
		private long acceptId = 0L;
		private final Object lock = new Object();

		private long getId() {
			synchronized (lock) {
				if (acceptId >= MAX_VALUE) {
					acceptId = 0L;
				}
				return ++acceptId;
			}
		}
	}
}
