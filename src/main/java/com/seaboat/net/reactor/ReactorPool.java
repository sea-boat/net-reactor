package com.seaboat.net.reactor;

import java.io.IOException;

import com.seaboat.net.reactor.handler.Handler;

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>Reactor pool. Socket connections are polling to the reactor of this pool. </p>
 */
public class ReactorPool {
	private final Reactor[] reactors;
	private volatile int nextReactor;
	private String name = "reactor";
	private BufferPool bufferPool;

	public ReactorPool(int poolSize, Handler handler) throws IOException {
		reactors = new Reactor[poolSize];
		for (int i = 0; i < poolSize; i++) {
			Reactor reactor = new Reactor(name + "-" + i, handler, this);
			reactors[i] = reactor;
			reactor.start();
		}
		this.bufferPool = new BufferPool(1024 * 1024 * 512, 10 * 1024);
	}

	public Reactor getNextReactor() {
		if (++nextReactor == reactors.length) {
			nextReactor = 0;
		}
		return reactors[nextReactor];
	}

	public BufferPool getBufferPool() {
		return bufferPool;
	}
}
