package com.seaboat.net.reactor.connection;

import java.nio.channels.SocketChannel;

import com.seaboat.net.reactor.Reactor;

/**
 * 
 * <pre><b>the default connection factory.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 */
public class DefaultConnectionFactory implements ConnectionFactory {

	public Connection createConnection(SocketChannel channel, long id,
			Reactor reactor) {
		return new Connection(channel, id, reactor);
	}

}
