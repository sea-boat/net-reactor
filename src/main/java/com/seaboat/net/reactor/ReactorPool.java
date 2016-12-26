package com.seaboat.net.reactor;

import java.io.IOException;

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

	public ReactorPool(int poolSize) throws IOException {
		reactors = new Reactor[poolSize];
		for (int i = 0; i < poolSize; i++) {
			Reactor reactor = new Reactor(name + "-" + i);
			reactors[i] = reactor;
			reactor.start();
		}
	}

	public Reactor getNextReactor() {
		if (++nextReactor == reactors.length) {
			nextReactor = 0;
		}
		return reactors[nextReactor];
	}
}
